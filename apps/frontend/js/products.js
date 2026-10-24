import { fetchCategories } from "./category-api.js";
import { request } from "./api.js";
import { hideState, showError, showLoading, showMessage } from "./ui-state.js";

const API_URL = "/api/products";
const SORT = "name,asc";

const totalCount = document.getElementById("total-count");
const listState = document.getElementById("list-state");
const productList = document.getElementById("product-list");
const categoryFilter = document.getElementById("category-filter");
const pageSizeSelect = document.getElementById("page-size");
const previousButton = document.getElementById("previous-button");
const nextButton = document.getElementById("next-button");
const pageIndicator = document.getElementById("page-indicator");

// Spring pages are zero-based; the page indicator shows one-based numbers.
// An empty categoryId means "all categories".
const state = {
  page: 0,
  size: Number(pageSizeSelect.value),
  categoryId: "",
  totalPages: 0,
};

const priceFormatter = new Intl.NumberFormat(undefined, { style: "currency", currency: "USD" });

function renderProduct(product) {
  const item = document.createElement("li");
  item.className = "product-item";

  if (product.imageUrl) {
    const image = document.createElement("img");
    image.className = "product-image";
    image.src = product.imageUrl;
    image.alt = product.name;
    image.loading = "lazy";
    image.addEventListener("error", () => image.remove());
    item.append(image);
  }

  const name = document.createElement("h3");
  name.textContent = product.name;

  const price = document.createElement("p");
  price.className = "product-price";
  price.textContent = priceFormatter.format(Number(product.price));

  const meta = document.createElement("p");
  meta.className = "product-meta";
  const stock = product.stock ?? 0;
  meta.textContent = [product.categoryName, `${stock} in stock`].filter(Boolean).join(" · ");

  item.append(name, price, meta);
  return item;
}

function renderPagination(data) {
  state.totalPages = data.totalPages;

  totalCount.textContent = `${data.totalElements} products`;
  totalCount.hidden = false;

  const currentPage = data.totalPages === 0 ? 0 : data.page + 1;
  pageIndicator.textContent = `Page ${currentPage} of ${data.totalPages}`;

  previousButton.disabled = data.page <= 0;
  nextButton.disabled = data.page + 1 >= data.totalPages;
}

function renderPage(data) {
  productList.replaceChildren(...data.content.map(renderProduct));

  renderPagination(data);

  if (data.content.length === 0) {
    showMessage(listState, "No products found.");
  } else {
    hideState(listState);
  }
}

async function loadCategoryOptions() {
  categoryFilter.disabled = true;
  categoryFilter.options[0].textContent = "Loading categories...";

  try {
    const categories = await fetchCategories();
    const options = categories.map((category) => {
      const option = document.createElement("option");
      option.value = String(category.id);
      option.textContent = category.name;
      return option;
    });
    categoryFilter.append(...options);
    categoryFilter.options[0].textContent = "All categories";
    categoryFilter.disabled = false;
  } catch {
    categoryFilter.disabled = true;
    categoryFilter.options[0].textContent = "Categories unavailable";
  }
}

// Only the most recent request may update the page, so a slow response
// for an old filter or page cannot overwrite the current results.
let latestRequestId = 0;

async function loadProducts() {
  const requestId = ++latestRequestId;

  showLoading(listState, "Loading products...");
  productList.replaceChildren();
  previousButton.disabled = true;
  nextButton.disabled = true;

  const params = new URLSearchParams({
    page: String(state.page),
    size: String(state.size),
    sort: SORT,
  });
  if (state.categoryId) {
    params.set("categoryId", state.categoryId);
  }

  try {
    const data = await request(`${API_URL}?${params}`);
    if (requestId === latestRequestId) {
      renderPage(data);
    }
  } catch (error) {
    if (requestId === latestRequestId) {
      totalCount.hidden = true;
      pageIndicator.textContent = "";
      showError(listState, `Could not load products: ${error.message}`, loadProducts);
    }
  }
}

previousButton.addEventListener("click", () => {
  if (state.page > 0) {
    state.page -= 1;
    loadProducts();
  }
});

nextButton.addEventListener("click", () => {
  if (state.page + 1 < state.totalPages) {
    state.page += 1;
    loadProducts();
  }
});

pageSizeSelect.addEventListener("change", () => {
  state.size = Number(pageSizeSelect.value);
  state.page = 0;
  loadProducts();
});

categoryFilter.addEventListener("change", () => {
  state.categoryId = categoryFilter.value;
  state.page = 0;
  loadProducts();
});

loadCategoryOptions();
loadProducts();
