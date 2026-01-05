<script setup>
import {ref, watch} from "vue"
import {useI18n} from "vue-i18n"
import {useInventoryStore} from "@/stores/inventoryStore.js"
import {StockOperationsType} from "@/utils/stockOperations.js"
import CloseIcon from "@/components/icons/CloseIcon.vue"

const {t} = useI18n()
const inventoryStore = useInventoryStore()

const props = defineProps({
  show: {type: Boolean, default: false},
  product: {type: Object, default: null}
})

const emit = defineEmits(['close', 'updated'])

const adjustmentType = ref(StockOperationsType.ADD)
const adjustmentQuantity = ref(1)

watch(() => props.show, (newVal) => {
  if (newVal) {
    adjustmentType.value = StockOperationsType.ADD
    adjustmentQuantity.value = 1
    inventoryStore.setError(null)
  }
})

function closeModal() {
  emit('close')
}

async function submitAdjustment() {
  if (!props.product || adjustmentQuantity.value <= 0) {
    inventoryStore.setError(t('validation.enterValidQuantity'))
    return
  }

  if (adjustmentType.value === StockOperationsType.REMOVE && adjustmentQuantity.value > (props.product.stock || 0)) {
    inventoryStore.setError(t('validation.cannotRemoveMore'))
    return
  }

  await inventoryStore.adjustStock({
    sku: props.product.sku,
    adjustmentQuantity: adjustmentQuantity.value,
    type: adjustmentType.value
  })

  if (!inventoryStore.error) {
    emit('updated')
    closeModal()
  }
}
</script>

<template>
  <div v-if="show" class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
       @click.self="closeModal">
    <div class="bg-white rounded-xl shadow-xl w-full max-w-md p-6">
      <div class="flex justify-between items-center mb-6">
        <h2 class="text-lg font-bold text-slate-900">{{ t('inventory.adjustStock') }}</h2>
        <button @click="closeModal" class="text-slate-400 hover:text-slate-600">
          <CloseIcon class="w-4 h-4 fill-current"/>
        </button>
      </div>

      <div class="mb-4">
        <p class="text-sm text-slate-600">
          {{ t('product.name') }}: <span class="font-medium text-slate-900">{{
            product?.name
          }}</span>
        </p>
        <p class="text-sm text-slate-600">
          {{ t('product.sku') }}: <span class="font-mono font-medium text-slate-900">{{
            product?.sku
          }}</span>
        </p>
        <p class="text-sm text-slate-600">
          {{ t('inventory.currentQuantity') }}: <span
          class="font-medium text-slate-900">{{ product?.stock ?? 0 }}</span>
        </p>
      </div>

      <div v-if="inventoryStore.error"
           class="p-3 bg-red-50 border border-red-200 text-red-700 rounded-lg mb-4 text-sm">
        {{ inventoryStore.error }}
      </div>

      <div class="space-y-4">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1">{{
              t('inventory.type')
            }}</label>
          <select
            v-model="adjustmentType"
            class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:border-blue-600 outline-none"
          >
            <option :value="StockOperationsType.ADD">{{ t('inventory.addStock') }}</option>
            <option :value="StockOperationsType.REMOVE">{{ t('inventory.removeStock') }}</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-slate-700 mb-1">{{
              t('inventory.quantity')
            }}</label>
          <input
            v-model.number="adjustmentQuantity"
            type="number"
            min="1"
            :placeholder="t('inventory.enterQuantity')"
            class="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:border-blue-600 outline-none"
          />
        </div>
      </div>

      <div class="flex gap-3 mt-6">
        <button
          @click="closeModal"
          class="flex-1 px-4 py-2 border border-slate-200 rounded-lg text-sm font-medium text-slate-600 hover:bg-slate-50 transition-colors"
        >
          {{ t('common.cancel') }}
        </button>
        <button
          @click="submitAdjustment"
          :disabled="inventoryStore.loading"
          class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg text-sm font-medium shadow-sm hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
        >
          <span v-if="inventoryStore.loading">{{ t('inventory.saving') }}</span>
          <span v-else>{{ t('common.save') }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

