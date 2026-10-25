// Runtime configuration for the container image.
// An empty base means same-origin requests ("/api/products"): Nginx proxies /api
// in Docker Compose and the AWS ALB routes /api/* to the backend service.
window.API_BASE_URL = "";
