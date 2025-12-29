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
  <div>
    <h1>Product Catalog</h1>
    <div class="search-container">
      <input
        v-model="searchInput"
        type="text"
        placeholder="Search products..."
        class="search-input"
      />
    </div>
    <div v-if="productStore.error" class="error">{{ productStore.error }}</div>
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
        {{ item.active ? 'Active' : 'Inactive' }}
      </template>
    </DataTable>
  </div>
</template>

<style scoped>
h1 {
  margin-bottom: 1rem;
}

.search-container {
  margin-bottom: 1rem;
}

.search-input {
  width: 100%;
  max-width: 300px;
  padding: 0.5rem 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.search-input:focus {
  outline: none;
  border-color: #007bff;
}

.error {
  color: red;
  padding: 1rem;
  background: #fee;
  margin-bottom: 1rem;
}
</style>
