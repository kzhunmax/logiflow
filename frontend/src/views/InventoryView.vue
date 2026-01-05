<script setup>
import {onMounted, ref} from "vue";
import {useInventoryStore} from "@/stores/inventoryStore.js";
import DataTable from "@/components/DataTable.vue";
import {StockOperationsType} from "@/utils/stockOperations.js";
import CloseIcon from "@/components/icons/CloseIcon.vue";

const inventoryStore = useInventoryStore()

const columns = [
  {key: 'sku', label: 'SKU'},
  {key: 'quantity', label: 'Quantity'},
  {key: 'status', label: 'Status'},
  {key: 'actions', label: 'Actions'},
]

const showModal = ref(false)
const selectedItem = ref(null)
const adjustmentType = ref(StockOperationsType.ADD)
const adjustmentQuantity = ref(0)

const searchInput = ref('')

function handlePageChange(page) {
  inventoryStore.fetchAllInventory(page)
}

function openAdjustModal(item) {
  selectedItem.value = item
  adjustmentType.value = StockOperationsType.ADD
  adjustmentQuantity.value = 0
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  selectedItem.value = null
  adjustmentType.value = StockOperationsType.ADD
  adjustmentQuantity.value = 0
}

async function submitAdjustment() {
  if (!selectedItem.value || adjustmentQuantity.value <= 0) {
    inventoryStore.setError('Please enter a valid quantity')
    return
  }

  await inventoryStore.adjustStock({
    sku: selectedItem.value.sku,
    adjustmentQuantity: adjustmentQuantity.value,
    type: adjustmentType.value
  })

  if (!inventoryStore.error) {
    closeModal()
  }
}

function getStockStatus(item) {
  const available = item.availableQuantity - (item.reserved || 0)
  return available <= 10 ? 'low' : 'in-stock'
}

onMounted(() => {
  inventoryStore.fetchAllInventory()
})
</script>

<template>
  <div class="w-full max-w-7xl mx-auto p-8 font-sans">

    <div class="flex justify-between items-end mb-8 flex-wrap gap-4">
      <div>
        <h1 class="text-2xl font-bold m-0">Inventory Management</h1>
        <p class="text-sm text-slate-500 mt-1 mb-0">Manage stock levels across all warehouses.</p>
      </div>

      <div class="flex gap-3 items-center">
        <div class="relative flex items-center">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"
               class="absolute left-3 w-4 h-4 fill-slate-400 pointer-events-none">
            <path
              d="M416 208c0 45.9-14.9 88.3-40 122.7L502.6 457.4c12.5 12.5 12.5 32.8 0 45.3s-32.8 12.5-45.3 0L330.7 376C296.3 401.1 253.9 416 208 416 93.1 416 0 322.9 0 208S93.1 0 208 0 416 93.1 416 208zM208 352a144 144 0 1 0 0-288 144 144 0 1 0 0 288z"/>
          </svg>
          <input
            v-model="searchInput"
            type="text"
            placeholder="Search SKU..."
            class="py-2 px-4 pl-9 border border-slate-200 rounded-lg text-sm w-64 outline-none transition-colors focus:border-blue-600"
          />
        </div>
      </div>
    </div>

    <div v-if="inventoryStore.error"
         class="p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg mb-6 text-sm">
      {{ inventoryStore.error }}
    </div>

    <DataTable
      :columns="columns"
      :items="inventoryStore.inventoryList"
      :loading="inventoryStore.loading"
      :current-page="inventoryStore.pagination.currentPage"
      :total-pages="inventoryStore.pagination.totalPages"
      :total-items="inventoryStore.pagination.totalItems"
      @page-change="handlePageChange"
    >
      <template #sku="{ item }">
        <span class="font-mono text-slate-600">{{ item.sku }}</span>
      </template>

      <template #quantity="{ item }">
        <span :class="getStockStatus(item) === 'low' ? 'font-bold text-red-600' : ''">
          {{ item.availableQuantity }}
        </span>
      </template>

      <template #status="{ item }">
        <span
          v-if="getStockStatus(item) === 'low'"
          class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-700"
        >
          Low Stock
        </span>
        <span
          v-else
          class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-700"
        >
          In Stock
        </span>
      </template>

      <template #actions="{ item }">
        <button
          @click.stop="openAdjustModal(item)"
          class="px-3 py-1 bg-blue-600 text-white rounded text-xs font-medium shadow-sm hover:bg-blue-700 transition-colors"
        >
          Adjust Stock
        </button>
      </template>
    </DataTable>

    <div v-if="showModal" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
         @click.self="closeModal">
      <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
        <div class="flex justify-between items-center mb-6">
          <h2 class="text-lg font-bold text-slate-900">Adjust Stock</h2>
          <button @click="closeModal" class="text-slate-400 hover:text-slate-600">
            <CloseIcon class="w-4 h-4 fill-current"/>
          </button>
        </div>

        <div class="mb-4">
          <p class="text-sm text-slate-600">
            SKU: <span class="font-mono font-medium text-slate-900">{{ selectedItem?.sku }}</span>
          </p>
          <p class="text-sm text-slate-600">
            Current Quantity: <span class="font-medium text-slate-900">{{
              selectedItem?.availableQuantity
            }}</span>
          </p>
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Type</label>
            <select
              v-model="adjustmentType"
              class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:border-blue-600 outline-none"
            >
              <option :value="StockOperationsType.ADD">Add Stock</option>
              <option :value="StockOperationsType.REMOVE">Remove Stock</option>
            </select>
          </div>

          <div>
            <label class="block text-sm font-medium text-slate-700 mb-1">Quantity</label>
            <input
              v-model.number="adjustmentQuantity"
              type="number"
              min="1"
              placeholder="Enter quantity"
              class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:border-blue-600 outline-none"
            />
          </div>
        </div>

        <div class="flex gap-3 mt-6">
          <button
            @click="closeModal"
            class="flex-1 px-4 py-2 border border-slate-200 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-50 transition-colors"
          >
            Cancel
          </button>
          <button
            @click="submitAdjustment"
            :disabled="inventoryStore.loading"
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium shadow-sm hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ inventoryStore.loading ? 'Saving...' : 'Submit' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
