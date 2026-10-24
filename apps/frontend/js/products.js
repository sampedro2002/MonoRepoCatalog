import { request } from "./api.js";

const API_URL = "/api/products";
const SORT = "name,asc";

const totalCount = document.getElementById("total-count");
const listStatus = document.getElementById("list-status");
const productList = document.getElementById("product-list");
const pageSizeSelect = document.getElementById("page-size");
const previousButton = document.getElementById("previous-button");
const nextButton = document.getElementById("next-button");
const pageIndicator = document.getElementById("page-indicator");

// Spring pages are zero-based; the page indicator shows one-based numbers.
const state = {
  page: 0,
  size: Number(pageSizeSelect.value),
  totalPages: 0,
};

const priceFormatter = new Intl.NumberFormat(undefined, { style: "currency", currency: "USD" });

function setListStatus(text, isError = false) {
  listStatus.textContent = text;
  listStatus.hidden = !text;
  listStatus.classList.toggle("message-error", isError);
}

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
    setListStatus("No products found.");
  } else {
    setListStatus("");
  }
}

async function loadProducts() {
  setListStatus("Loading products...");
  productList.replaceChildren();
  previousButton.disabled = true;
  nextButton.disabled = true;

  const params = new URLSearchParams({
    page: String(state.page),
    size: String(state.size),
    sort: SORT,
  });

  try {
    renderPage(await request(`${API_URL}?${params}`));
  } catch (error) {
    totalCount.hidden = true;
    pageIndicator.textContent = "";
    setListStatus(`Could not load products: ${error.message}`, true);
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

loadProducts();
