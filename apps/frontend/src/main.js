const statusEl = document.getElementById('backend-status');

// Goes through the Vite proxy to http://localhost:8080/actuator/health
async function loadBackendHealth() {
  try {
    const response = await fetch('/actuator/health');
    const body = await response.json();
    statusEl.textContent = body.status;
  } catch {
    statusEl.textContent = 'no disponible';
  }
}

loadBackendHealth();
