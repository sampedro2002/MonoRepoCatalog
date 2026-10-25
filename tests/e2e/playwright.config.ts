import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './api',
  // Tests share one backend and create/delete their own data, so run them one at a time
  fullyParallel: false,
  workers: 1,
  retries: 0,
  reporter: [['list'], ['html', { open: 'never' }]],
  use: {
    // Base URL of the running backend. CI sets API_URL from suitest.config.json
    baseURL: process.env.API_URL ?? 'http://localhost:8080',
    extraHTTPHeaders: { Accept: 'application/json' },
  },
});
