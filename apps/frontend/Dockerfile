FROM nginx:1.27-alpine

# Drop the stock site, ours listens on the same file name
RUN rm /etc/nginx/conf.d/default.conf

COPY nginx.conf /etc/nginx/conf.d/

COPY index.html /usr/share/nginx/html/
COPY rick-morty.html /usr/share/nginx/html/
COPY css/ /usr/share/nginx/html/css/
COPY js/ /usr/share/nginx/html/js/

# The container talks to the API through same-origin relative URLs (/api/...),
# so the dev config.js is replaced by the Docker one, renamed to config.js.
COPY config.docker.js /usr/share/nginx/html/config.js

EXPOSE 80
# 127.0.0.1, not localhost: BusyBox wget prefers ::1 and nginx listens on IPv4 only
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget -qO- http://127.0.0.1/ || exit 1
