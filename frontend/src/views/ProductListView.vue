<script setup>
import {useRouter} from "vue-router";
import {useProductStore} from "@/stores/productStore.js";
import {onMounted} from "vue";
import DataTable from "@/components/DataTable.vue";

const router = useRouter()
const productStore = useProductStore()

const columns = [
  {key: 'name', label: 'Name'},
  {key: 'price', label: 'Price'},
  {key: 'quantity', label: 'Stock'},
  {key: 'active', label: 'Status'},
]

function handlePageChange(page) {
  productStore.fetchProducts(page)
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

.error {
  color: red;
  padding: 1rem;
  background: #fee;
  margin-bottom: 1rem;
}
</style>
