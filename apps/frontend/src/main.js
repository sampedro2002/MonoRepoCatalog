const statusEl = document.getElementById('backend-status');

// Consulta el estado mediante el proxy de Vite en http://localhost:8080/actuator/health
async function cargarEstadoBackend() {
  try {
    const response = await fetch('/actuator/health');
    const body = await response.json();
    statusEl.textContent = body.status;
  } catch {
    statusEl.textContent = 'no disponible';
  }
}

cargarEstadoBackend();
