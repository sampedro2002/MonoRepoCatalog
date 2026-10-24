// Helpers for the loading, empty and error states shown above a list.
// Each helper replaces the whole content of the state region.

export function showLoading(region, text = "Loading...") {
  const spinner = document.createElement("span");
  spinner.className = "spinner";
  spinner.setAttribute("aria-hidden", "true");

  region.className = "state";
  region.setAttribute("role", "status");
  region.replaceChildren(spinner, text);
  region.hidden = false;
}

export function showError(region, message, onRetry) {
  region.className = "state state-error";
  region.setAttribute("role", "alert");
  region.replaceChildren(message);

  if (onRetry) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = "button";
    button.textContent = "Retry";
    button.addEventListener("click", onRetry);
    region.append(button);
  }

  region.hidden = false;
}

export function showMessage(region, text) {
  region.className = "state";
  region.setAttribute("role", "status");
  region.replaceChildren(text);
  region.hidden = false;
}

export function hideState(region) {
  region.replaceChildren();
  region.hidden = true;
}
