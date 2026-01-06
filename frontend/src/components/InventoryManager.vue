<script setup>

import {useInventoryStore} from "@/stores/inventoryStore.js";
import {computed, onMounted, ref} from "vue";
import {useI18n} from "vue-i18n";
import ChevronDownIcon from "@/components/icons/ChevronDownIcon.vue";
import {StockOperationsType} from "@/utils/stockOperations.js";

const {t} = useI18n()

const props = defineProps({
  sku: {type: String, required: true},
})

const store = useInventoryStore()
const quantity = ref(1)
const operation = ref(StockOperationsType.ADD)

onMounted(() => {
  store.fetchStock(props.sku)
})

const availableQuantity = computed(() => {
  return store.stock?.availableQuantity ?? 0
})

async function handleAdjustment() {
  if (quantity.value <= 0) {
    store.setError(t('validation.quantityGreaterZero'))
    return
  }

  if (operation.value === StockOperationsType.REMOVE && quantity.value > availableQuantity.value) {
    store.setError(t('validation.cannotRemoveMore'))
    return
  }

  const payload = {
    sku: props.sku,
    adjustmentQuantity: quantity.value,
    type: operation.value
  }

  try {
    await store.adjustStock(payload)
    await store.fetchStock(props.sku)
    quantity.value = 1
  } catch (err) {
    // Error is already set in the store
  }
}
</script>

<template>
  <div class="bg-slate-50 rounded-lg border border-slate-200 p-6">
    <h3
      class="text-xs uppercase tracking-wide text-slate-400 font-bold mb-4 border-b border-slate-200 pb-2">
      {{ t('inventory.title') }}</h3>

    <div v-if="store.loading && !store.stock" class="text-center text-slate-500 p-4">
      {{ t('inventory.loadingInventory') }}
    </div>

    <div v-else class="flex flex-col gap-4">
      <div class="bg-white border border-slate-200 rounded-lg p-4 text-center">
        <p class="text-sm text-slate-500 mb-1">{{ t('inventory.availableQuantity') }}</p>
        <p class="text-3xl font-bold"
           :class="availableQuantity < 10 ? 'text-red-700' : 'text-green-700'">
          {{ availableQuantity }}
        </p>
      </div>

      <div v-if="store.error"
           class="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm">
        {{ store.error }}
      </div>

      <div class="flex flex-col gap-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="relative flex flex-col gap-2">
            <label class="text-sm font-medium text-slate-600">{{ t('inventory.operation') }}</label>
            <div class="relative">
              <select v-model="operation"
                      class="w-full px-2.5 py-2.5 border border-slate-200 rounded-lg text-sm bg-white h-10 appearance-none pr-8 cursor-pointer focus:outline-2 focus:outline-blue-600 focus:-outline-offset-1">
                <option :value="StockOperationsType.ADD">{{ t('inventory.addStock') }}</option>
                <option :value="StockOperationsType.REMOVE">{{ t('inventory.removeStock') }}</option>
              </select>
              <div class="absolute right-2.5 inset-y-0 flex items-center pointer-events-none">
                <ChevronDownIcon class="w-3 h-3 fill-slate-500"/>
              </div>
            </div>
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-sm font-medium text-slate-600">{{ t('inventory.quantity') }}</label>
            <input
              v-model.number="quantity"
              type="number"
              min="1"
              class="w-full px-2.5 py-2.5 border border-slate-200 rounded-lg text-sm bg-white h-10 focus:outline-2 focus:outline-blue-600 focus:-outline-offset-1"
              :placeholder="t('inventory.enterQuantity')"
            />
          </div>
        </div>

        <button
          @click="handleAdjustment"
          :disabled="store.loading || quantity <= 0"
          class="px-4 py-3 rounded-lg text-sm font-semibold border-none cursor-pointer transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          :class="operation === StockOperationsType.ADD ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-red-700 text-white hover:opacity-90'"
        >
          <span v-if="store.loading">{{ t('inventory.processing') }}</span>
          <span v-else>{{
              operation === StockOperationsType.ADD ? t('inventory.addStock') : t('inventory.removeStock')
            }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>
