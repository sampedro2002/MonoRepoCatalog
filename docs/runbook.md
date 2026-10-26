# Operations Runbook - Catalog

Production is one EC2 host (`catalog-app`) running `docker-compose.prod.yml` from `/opt/catalog`. The public URL is `http://<EC2_HOST>`, where `EC2_HOST` is the Elastic IP (`terraform output public_ip`).

Connect with the Learner Lab key:

```bash
ssh -i labsuser.pem ec2-user@<EC2_HOST>
cd /opt/catalog
```

## 1. See the deployed version

```bash
docker compose ps                                    # state and health of each service
docker compose images                                # image and tag of each service
docker inspect --format '{{index .RepoDigests 0}}' "$(docker compose images -q backend)"
```

The digest matches a `release-<sha>` tag in Docker Hub.

## 2. Roll back

If the latest release fails, run the **Manual Rollback** workflow in GitHub Actions:

* **release**: tag `release-<previous_sha>`
* **service**: `backend` or `frontend`

It retags that image as `:prod` and runs **Deploy to EC2**, which pulls it and restarts only that service.

## 3. Logs

```bash
docker compose logs -f backend                       # follow the API
docker compose logs --since 1h backend | grep '<trace-id>'
```

Every API error carries a `traceId`, also returned in the `X-Trace-Id` response header. Search for it in the backend log. Logs rotate at 3 x 10 MB per container.

## 4. The site is down

1. Is the lab running? When the Learner Lab session ends, AWS stops the instance. Start the lab again and wait 2 to 3 minutes. Docker and the containers come back by themselves, with the same IP.
2. `docker compose ps`: a service that is not `healthy` shows why in `docker compose logs <service>`.
3. `docker compose up -d` restarts anything that is stopped.
4. First boot problems (Docker install, first pull) are in `/var/log/cloud-init-output.log`.

## 5. Database

Postgres runs in the `postgres` container, with its data in the `pgdata` volume on the instance disk. It survives restarts and stops, but **`terraform destroy` deletes it**. Back it up first if needed:

```bash
docker compose exec postgres pg_dump -U catalog catalog > backup.sql
```

Flyway applies migrations when the backend starts. If one fails, never edit a migration that was already applied. Add a new version (`V2__...`) with the fix.
