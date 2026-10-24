import { ApiError } from "./api.js";
import { createCategory, deleteCategory, fetchCategories, updateCategory } from "./category-api.js";

const form = document.getElementById("category-form");
const idInput = document.getElementById("category-id");
const nameInput = document.getElementById("category-name");
const descriptionInput = document.getElementById("category-description");
const formTitle = document.getElementById("form-title");
const saveButton = document.getElementById("save-button");
const cancelButton = document.getElementById("cancel-button");
const formError = document.getElementById("form-error");
const listStatus = document.getElementById("list-status");
const categoryList = document.getElementById("category-list");

function setListStatus(text, isError = false) {
  listStatus.textContent = text;
  listStatus.hidden = !text;
  listStatus.classList.toggle("message-error", isError);
}

function showFormError(text) {
  formError.textContent = text;
  formError.hidden = false;
}

function clearFormError() {
  formError.textContent = "";
  formError.hidden = true;
}

function resetForm() {
  form.reset();
  idInput.value = "";
  formTitle.textContent = "New category";
  saveButton.textContent = "Create";
  cancelButton.hidden = true;
  clearFormError();
}

function startEditing(category) {
  idInput.value = category.id;
  nameInput.value = category.name;
  descriptionInput.value = category.description ?? "";
  formTitle.textContent = "Edit category";
  saveButton.textContent = "Update";
  cancelButton.hidden = false;
  clearFormError();
  nameInput.focus();
}

function createButton(label, className, onClick) {
  const button = document.createElement("button");
  button.type = "button";
  button.className = `button ${className}`;
  button.textContent = label;
  button.addEventListener("click", onClick);
  return button;
}

function renderCategory(category) {
  const item = document.createElement("li");
  item.className = "category-item";

  const info = document.createElement("div");
  info.className = "category-info";

  const name = document.createElement("h3");
  name.textContent = category.name;
  info.append(name);

  if (category.description) {
    const description = document.createElement("p");
    description.textContent = category.description;
    info.append(description);
  }

  const buttons = document.createElement("div");
  buttons.className = "category-buttons";
  buttons.append(
    createButton("Edit", "", () => startEditing(category)),
    createButton("Delete", "button-danger", () => handleDelete(category)),
  );

  item.append(info, buttons);
  return item;
}

function renderList(categories) {
  categoryList.replaceChildren(...categories.map(renderCategory));

  if (categories.length === 0) {
    setListStatus("No categories yet. Create the first one using the form.");
  } else {
    setListStatus("");
  }
}

async function loadCategories() {
  setListStatus("Loading categories...");
  categoryList.replaceChildren();

  try {
    renderList(await fetchCategories());
  } catch (error) {
    setListStatus(`Could not load categories: ${error.message}`, true);
  }
}

async function saveCategory(event) {
  event.preventDefault();
  clearFormError();

  const payload = {
    name: nameInput.value.trim(),
    description: descriptionInput.value.trim() || null,
  };

  if (!payload.name) {
    showFormError("Name is required.");
    nameInput.focus();
    return;
  }

  const id = idInput.value;
  const isEditing = id !== "";

  saveButton.disabled = true;
  try {
    if (isEditing) {
      await updateCategory(id, payload);
    } else {
      await createCategory(payload);
    }
    resetForm();
    await loadCategories();
  } catch (error) {
    const message = error instanceof ApiError && error.status === 409
      ? "A category with this name already exists."
      : error.message;
    showFormError(`Could not save the category: ${message}`);
  } finally {
    saveButton.disabled = false;
  }
}

async function handleDelete(category) {
  if (!window.confirm(`Delete the category "${category.name}"?`)) {
    return;
  }

  try {
    await deleteCategory(category.id);
    if (idInput.value === String(category.id)) {
      resetForm();
    }
    await loadCategories();
  } catch (error) {
    setListStatus(`Could not delete the category: ${error.message}`, true);
  }
}

form.addEventListener("submit", saveCategory);
cancelButton.addEventListener("click", resetForm);

loadCategories();
