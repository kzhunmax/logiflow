<script setup>
import {useRouter} from "vue-router";
import {useProductStore} from "@/stores/productStore.js";
import {onMounted, ref, watch} from "vue";
import DataTable from "@/components/DataTable.vue";

const router = useRouter()
const productStore = useProductStore()

const searchInput = ref('')
let debounceTimer = null

const columns = [
  {key: 'name', label: 'Name'},
  {key: 'price', label: 'Price'},
  {key: 'quantity', label: 'Stock'},
  {key: 'active', label: 'Status'},
]

function handleSearch(value) {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    productStore.fetchProducts(1, value)
  }, 300)
}

watch(searchInput, (newValue) => {
  handleSearch(newValue)
})

function handlePageChange(page) {
  productStore.fetchProducts(page, searchInput.value)
}

function handleRowClick(product) {
  router.push(`/product/${product.id}`)
}

onMounted(() => {
  productStore.fetchProducts()
})

</script>

<template>
  <div class="page-wrapper">

    <div class="page-header">
      <div class="header-content">
        <h1>Product Catalog</h1>
        <p class="subtitle">Manage your product inventory and prices.</p>
      </div>

      <div class="header-actions">
        <div class="search-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><path d="M416 208c0 45.9-14.9 88.3-40 122.7L502.6 457.4c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L330.7 376C296.3 401.1 253.9 416 208 416 93.1 416 0 322.9 0 208S93.1 0 208 0 416 93.1 416 208zM208 352a144 144 0 1 0 0-288 144 144 0 1 0 0 288z"/></svg>
          <input
            v-model="searchInput"
            type="text"
            placeholder="Search products..."
            class="search-input"
          />
        </div>

        <router-link to="products/create" class="btn-primary">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"><path d="M256 64c0-17.7-14.3-32-32-32s-32 14.3-32 32l0 160-160 0c-17.7 0-32 14.3-32 32s14.3 32 32 32l160 0 0 160c0 17.7 14.3 32 32 32s32-14.3 32-32l0-160 160 0c17.7 0 32-14.3 32-32s-14.3-32-32-32l-160 0 0-160z"/></svg>
          Create New
        </router-link>
      </div>
    </div>

    <div v-if="productStore.error" class="error-alert">
      {{ productStore.error }}
    </div>

    <DataTable
      :columns="columns"
      :items="productStore.products"
      :loading="productStore.loading"
      :current-page="productStore.currentPage"
      :total-pages="productStore.totalPages"
      :total-items="productStore.totalItems"
      @page-change="handlePageChange"
      @row-click="handleRowClick"
    >
      <template #price="{ item }">
        ${{ item.price?.toFixed(2) }}
      </template>

      <template #active="{ item }">
        <span :class="['badge', item.active ? 'badge-green' : 'badge-gray']">
          {{ item.active ? 'In Stock' : 'Inactive' }}
        </span>
      </template>
    </DataTable>

  </div>
</template>

<style scoped>
.page-wrapper {
  width: 100%;
  max-width: 80rem;
  margin: 0 auto;
  padding: 2rem;
  font-family: ui-sans-serif, system-ui, -apple-system, sans-serif;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-content h1 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
}

.subtitle {
  font-size: 0.875rem;
  color: #64748b;
  margin-top: 0.25rem;
  margin-bottom: 0;
}

.header-actions {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.search-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-wrapper svg {
  position: absolute;
  left: 0.75rem;
  width: 1rem;
  height: 1rem;
  fill: var(--color-text-secondary);
  pointer-events: none;
}

.search-input {
  padding: 0.5rem 1rem 0.5rem 2.25rem;
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  width: 16rem;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.search-input:focus {
  border-color: var(--color-primary);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background-color: var(--color-primary);
  color: var(--color-white);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  transition: background-color 0.2s;
  border: none;
}

.btn-primary svg {
  width: 0.875rem;
  height: 0.875rem;
  fill: currentColor;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.error-alert {
  padding: 1rem;
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-text-error);
  border-radius: 0.5rem;
  margin-bottom: 1.5rem;
  font-size: 0.875rem;
}

.badge {
  display: inline-flex;
  padding: 0.125rem 0.625rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}
.badge-green { background-color: #dcfce7; color: #166534; }
.badge-gray  { background-color: #f1f5f9; color: #475569; }
</style>
