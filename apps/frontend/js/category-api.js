import { request } from "./api.js";

const API_URL = "/api/categories";

// The backend exposes categories with Spanish JSON field names (nombre, descripcion).
// They are mapped to English here, so the rest of the frontend only uses English names.
function fromApi(category) {
  return {
    id: category.id,
    name: category.nombre,
    description: category.descripcion ?? null,
  };
}

function toApi(category) {
  return {
    nombre: category.name,
    descripcion: category.description,
  };
}

export async function fetchCategories() {
  const categories = await request(API_URL);
  return categories.map(fromApi);
}

export function createCategory(category) {
  return request(API_URL, { method: "POST", body: JSON.stringify(toApi(category)) });
}

export function updateCategory(id, category) {
  return request(`${API_URL}/${encodeURIComponent(id)}`, {
    method: "PUT",
    body: JSON.stringify(toApi(category)),
  });
}

export function deleteCategory(id) {
  return request(`${API_URL}/${encodeURIComponent(id)}`, { method: "DELETE" });
}
