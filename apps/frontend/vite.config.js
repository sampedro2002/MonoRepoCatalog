import { defineConfig } from 'vite';

// The Spring Boot backend runs on 8080 (apps/backend). In development the
// browser talks to the Vite server on 5500 and Vite forwards the calls, so the
// frontend never needs CORS.
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
