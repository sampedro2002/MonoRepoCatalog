import { defineConfig } from 'vite';

// The Spring Boot backend runs on port 8080 (apps/backend). During development,
// the browser uses Vite on port 5500, and Vite forwards the API calls,
// so the frontend does not need CORS.
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
