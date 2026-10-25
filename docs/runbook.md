# Runbook de Operaciones - Catalog

## 1. Ver la versión desplegada
Para ver qué imagen está corriendo en producción:
```bash
aws ecs describe-services --cluster catalog --services backend --query 'services[0].taskDefinition'
# Luego busca el ARN del task definition para ver la imagen (digest).
```

## 2. Hacer un Rollback
Si la última versión en producción falla, ve a GitHub Actions y ejecuta el workflow Manual Rollback:

* **release**: tag `release-<sha_anterior>`
* **service**: `backend` o `frontend`

Esto re-etiquetará la imagen anterior como `:prod` y forzará el redespliegue en ECS.

## 3. Revisar Logs
Los logs se envían a CloudWatch en los grupos `/ecs/catalog-backend` y `/ecs/catalog-frontend`.

Para buscar un error específico, busca por el header `X-Trace-Id` que devolvió la API al cliente.

**Query en CloudWatch Logs Insights:**
```text
fields @timestamp, message, traceId | filter traceId = "aqui-el-id"
```

## 4. Base de Datos
Flyway maneja las migraciones al arrancar el contenedor. Si hay un error, nunca edites una migración ya aplicada. Crea una nueva versión (`V2__...`) con la corrección.
