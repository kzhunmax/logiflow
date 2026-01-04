<script setup>

import {useInventoryStore} from "@/stores/inventoryStore.js";
import {computed, onMounted, ref} from "vue";
import ChevronDownIcon from "@/components/icons/ChevronDownIcon.vue";

const props = defineProps({
  sku: {type: String, required: true},
})

const store = useInventoryStore()
const quantity = ref(1)
const operation = ref('ADD')

onMounted(() => {
  store.fetchStock(props.sku)
})

const availableQuantity = computed(() => {
  return store.stock?.availableQuantity ?? 0
})

async function handleAdjustment() {
  if (quantity.value <= 0) {
    store.setError('Quantity must be greater than zero.')
    return
  }

  if (operation.value === 'REMOVE' && quantity.value > availableQuantity.value) {
    store.setError('Cannot remove more than available stock.')
    return
  }

  const payload = {
    sku: props.sku,
    adjustmentQuantity: quantity.value,
    type: operation.value
  }

  await store.adjustStock(payload)

  if (!store.error) {
    quantity.value = 1
  }
}
</script>

<template>
  <div class="bg-slate-50 rounded-lg border border-slate-200 p-6">
    <h3 class="text-xs uppercase tracking-wide text-slate-400 font-bold mb-4 border-b border-slate-200 pb-2">Inventory Management</h3>

    <div v-if="store.loading && !store.stock" class="text-center text-slate-500 p-4">
      Loading inventory...
    </div>

    <div v-else class="flex flex-col gap-4">
      <div class="bg-white border border-slate-200 rounded-lg p-4 text-center">
        <p class="text-sm text-slate-500 mb-1">Available Quantity</p>
        <p class="text-3xl font-bold" :class="availableQuantity < 10 ? 'text-red-700' : 'text-green-700'">
          {{ availableQuantity }}
        </p>
      </div>

      <div v-if="store.error" class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
        {{ store.error }}
      </div>

      <div class="flex flex-col gap-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium text-slate-600">Operation</label>
            <select v-model="operation" class="w-full px-2.5 py-2.5 border border-slate-200 rounded-lg text-sm bg-white h-10 appearance-none bg-[url('data:image/svg+xml,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%2212%22%20height%3D%2212%22%20viewBox%3D%220%200%2012%2012%22%3E%3Cpath%20fill%3D%22%23475569%22%20d%3D%22M6%208L1%203h10z%22%2F%3E%3C%2Fsvg%3E')] bg-no-repeat bg-position-[right_0.625rem_center] pr-8 cursor-pointer focus:outline-2 focus:outline-blue-600 focus:-outline-offset-1">
              <option value="ADD">Add Stock</option>
              <option value="REMOVE">Remove Stock</option>
            </select>
            <ChevronDownIcon class="absolute right-2.5 top-1/2 -translate-y-1/2 w-3 h-3 fill-slate-500 pointer-events-none" />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium text-slate-600">Quantity</label>
            <input
              v-model.number="quantity"
              type="number"
              min="1"
              class="w-full px-2.5 py-2.5 border border-slate-200 rounded-lg text-sm bg-white h-10 focus:outline-2 focus:outline-blue-600 focus:-outline-offset-1"
              placeholder="Enter quantity"
            />
          </div>
        </div>

        <button
          @click="handleAdjustment"
          :disabled="store.loading || quantity <= 0"
          class="px-4 py-3 rounded-lg text-sm font-semibold border-none cursor-pointer transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          :class="operation === 'ADD' ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-red-700 text-white hover:opacity-90'"
        >
          <span v-if="store.loading">Processing...</span>
          <span v-else>{{ operation === 'ADD' ? 'Add Stock' : 'Remove Stock' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>

