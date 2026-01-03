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
  {key: 'sku', label: 'SKU'},
  {key: 'price', label: 'Price'},
  {key: 'category', label: 'Category'},
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
  <div class="w-full max-w-7xl mx-auto p-8 font-sans">

    <div class="flex justify-between items-end mb-8 flex-wrap gap-4">
      <div>
        <h1 class="text-2xl font-bold m-0">Product Catalog</h1>
        <p class="text-sm text-slate-500 mt-1 mb-0">Manage your product inventory and prices.</p>
      </div>

      <div class="flex gap-3 items-center">
        <div class="relative flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512" class="absolute left-3 w-4 h-4 fill-slate-400 pointer-events-none"><path d="M416 208c0 45.9-14.9 88.3-40 122.7L502.6 457.4c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L330.7 376C296.3 401.1 253.9 416 208 416 93.1 416 0 322.9 0 208S93.1 0 208 0 416 93.1 416 208zM208 352a144 144 0 1 0 0-288 144 144 0 1 0 0 288z"/></svg>
          <input
            v-model="searchInput"
            type="text"
            placeholder="Search products..."
            class="py-2 px-4 pl-9 border border-slate-200 rounded-lg text-sm w-64 outline-none transition-colors focus:border-blue-600"
          />
        </div>

        <router-link to="products/create" class="inline-flex items-center gap-2 py-2 px-4 bg-blue-600 text-white rounded-lg text-sm font-medium no-underline shadow-sm transition-colors hover:bg-blue-700">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="w-3.5 h-3.5 fill-current"><path d="M256 64c0-17.7-14.3-32-32-32s-32 14.3-32 32l0 160-160 0c-17.7 0-32 14.3-32 32s14.3 32 32 32l160 0 0 160c0 17.7 14.3 32 32 32s32-14.3 32-32l0-160 160 0c17.7 0 32-14.3 32-32s-14.3-32-32-32l-160 0 0-160z"/></svg>
          Create New
        </router-link>
      </div>
    </div>

    <div v-if="productStore.error" class="p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg mb-6 text-sm">
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

      <template #category="{ item }">
        {{ item.category?.name || '-' }}
      </template>
    </DataTable>

  </div>
</template>

<style scoped>

</style>
