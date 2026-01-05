<script setup>
import {useRouter} from "vue-router";
import {useProductStore} from "@/stores/productStore.js";
import {ref, watch} from "vue";
import {useI18n} from "vue-i18n";
import DataTable from "@/components/DataTable.vue";
import SearchIcon from "@/components/icons/SearchIcon.vue";
import PlusIcon from "@/components/icons/PlusIcon.vue";

const { t } = useI18n()
const router = useRouter()
const productStore = useProductStore()
if (!productStore.products.length) {
  productStore.fetchProducts()
}

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
