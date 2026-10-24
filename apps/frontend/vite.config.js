import { defineConfig } from 'vite';

// El backend Spring Boot se ejecuta en 8080 (apps/backend). Durante el
// desarrollo, el navegador usa Vite en 5500 y Vite reenvia las llamadas,
// por lo que el frontend no necesita CORS.
const BACKEND_URL = 'http://localhost:8080';

export default defineConfig({
  server: {
    port: 5500,
    proxy: {
      '/api': { target: BACKEND_URL, changeOrigin: true },
      '/actuator': { target: BACKEND_URL, changeOrigin: true },
    },
  },
});
