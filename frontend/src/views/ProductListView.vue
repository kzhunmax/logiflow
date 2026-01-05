<script setup>
import {useRouter} from "vue-router";
import {useProductStore} from "@/stores/productStore.js";
import {useInventoryStore} from "@/stores/inventoryStore.js";
import {computed, ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import DataTable from "@/components/DataTable.vue";
import SearchIcon from "@/components/icons/SearchIcon.vue";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import StockAdjustmentModal from "@/components/StockAdjustmentModal.vue";

const {t} = useI18n()
const router = useRouter()
const productStore = useProductStore()
const inventoryStore = useInventoryStore()

const searchInput = ref('')
let debounceTimer = null

const showStockModal = ref(false)
const selectedProduct = ref(null)

const columns = [
  {key: 'name', label: 'Name'},
  {key: 'sku', label: 'SKU'},
  {key: 'price', label: 'Price'},
  {key: 'stock', label: 'Stock'},
  {key: 'actions', label: 'Actions'},
]

const productsWithStock = computed(() => {
  return productStore.products.map(product => ({
    ...product,
    stock: inventoryStore.getStockBySku(product.sku)
  }))
})

async function fetchData(page = 1, search = searchInput.value) {
  await productStore.fetchProducts(page, search)
  const skus = productStore.products.map(p => p.sku)
  await inventoryStore.fetchInventoryBySKUs(skus)
}

if (!productStore.products.length) {
  fetchData()
}

function handleSearch(value) {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    fetchData(1, value)
  }, 300)
}

watch(searchInput, (newValue) => {
  handleSearch(newValue)
})

function handlePageChange(page) {
  fetchData(page, searchInput.value)
}

function handleRowClick(product) {
  router.push(`/product/${product.id}`)
}

function openStockModal(product) {
  selectedProduct.value = product
  showStockModal.value = true
}

function closeStockModal() {
  showStockModal.value = false
  selectedProduct.value = null
}

async function handleStockUpdated() {
  const skus = productStore.products.map(p => p.sku)
  await inventoryStore.fetchInventoryBySKUs(skus)
}

function getStockStatus(stock) {
  if (stock === 0) return 'out'
  if (stock <= 10) return 'low'
  return 'ok'
}
</script>

<template>
  <div class="w-full max-w-7xl mx-auto p-8 font-sans">

    <div class="flex justify-between items-end mb-8 flex-wrap gap-4">
      <div>
        <h1 class="text-2xl font-bold m-0">{{ t('product.catalog') }}</h1>
        <p class="text-sm text-slate-500 mt-1 mb-0">{{ t('product.catalogDescription') }}</p>
      </div>

      <div class="flex gap-3 items-center">
        <div class="relative flex items-center">
          <SearchIcon class="absolute left-3 w-4 h-4 fill-slate-400 pointer-events-none"/>
          <input
            v-model="searchInput"
            type="text"
            :placeholder="t('product.searchProducts')"
            class="py-2 px-4 pl-9 border border-slate-200 rounded-lg text-sm w-64 outline-none transition-colors focus:border-blue-600"
          />
        </div>

        <router-link to="products/create"
                     class="inline-flex items-center gap-2 py-2 px-4 bg-blue-600 text-white rounded-lg text-sm font-medium no-underline shadow-sm transition-colors hover:bg-blue-700">
          <PlusIcon class="w-3.5 h-3.5 fill-current"/>
          {{ t('product.createNew') }}
        </router-link>
      </div>
    </div>

    <div v-if="productStore.error"
         class="p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg mb-6 text-sm">
      {{ productStore.error }}
    </div>

    <DataTable
      :columns="columns"
      :items="productsWithStock"
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

      <template #stock="{ item }">
        <span
          :class="{
            'text-red-600 font-semibold': getStockStatus(item.stock) === 'out',
            'text-amber-600 font-medium': getStockStatus(item.stock) === 'low',
            'text-slate-900': getStockStatus(item.stock) === 'ok'
          }"
        >
          {{ item.stock }}
        </span>
        <span
          v-if="getStockStatus(item.stock) === 'low'"
          class="ml-2 inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium bg-amber-100 text-amber-700"
        >
          {{ t('inventory.lowStock') }}
        </span>
        <span
          v-else-if="getStockStatus(item.stock) === 'out'"
          class="ml-2 inline-flex items-center px-1.5 py-0.5 rounded text-xs font-medium bg-red-100 text-red-700"
        >
          {{ t('inventory.outOfStock') }}
        </span>
      </template>

      <template #actions="{ item }">
        <button
          @click.stop="openStockModal(item)"
          class="px-3 py-1.5 bg-slate-100 text-slate-700 rounded text-xs font-medium hover:bg-slate-200 transition-colors"
        >
          {{ t('inventory.adjustStock') }}
        </button>
      </template>
    </DataTable>

    <StockAdjustmentModal
      :show="showStockModal"
      :product="selectedProduct"
      @close="closeStockModal"
      @updated="handleStockUpdated"
    />
  </div>
</template>

