export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

export async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    ...options,
  });

  if (!response.ok) {
    throw new ApiError(response.status, await readErrorMessage(response));
  }

  return response.status === 204 ? null : response.json();
}

async function readErrorMessage(response) {
  try {
    const body = await response.json();
    if (body && body.message) {
      return body.message;
    }
  } catch {
    // The body is not JSON; fall back to the status text below.
  }

  return response.statusText || `Request failed with status ${response.status}`;
}
