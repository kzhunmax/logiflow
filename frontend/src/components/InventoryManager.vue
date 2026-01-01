<script setup>

import {useInventoryStore} from "@/stores/inventoryStore.js";
import {computed, onMounted, ref} from "vue";

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
  <div class="inventory-manager">
    <h3 class="section-title">Inventory Management</h3>

    <div v-if="store.loading && !store.stock" class="loading-state">
      Loading inventory...
    </div>

    <div v-else class="inventory-content">
      <div class="stock-display">
        <p class="stock-label">Available Quantity</p>
        <p class="stock-value" :class="{'low-stock': availableQuantity < 10}">
          {{ availableQuantity }}
        </p>
      </div>

      <div v-if="store.error" class="error-message">
        {{ store.error }}
      </div>

      <div class="adjustment-form">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">Operation</label>
            <select v-model="operation" class="form-select">
              <option value="ADD">Add Stock</option>
              <option value="REMOVE">Remove Stock</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">Quantity</label>
            <input
              v-model.number="quantity"
              type="number"
              min="1"
              class="form-input"
              placeholder="Enter quantity"
            />
          </div>
        </div>

        <button
          @click="handleAdjustment"
          :disabled="store.loading || quantity <= 0"
          class="btn-adjust"
          :class="operation === 'ADD' ? 'btn-add' : 'btn-remove'"
        >
          <span v-if="store.loading">Processing...</span>
          <span v-else>{{ operation === 'ADD' ? 'Add Stock' : 'Remove Stock' }}</span>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.inventory-manager {
  background-color: var(--color-background);
  border-radius: 0.5rem;
  border: 1px solid var(--color-border);
  padding: 1.5rem;
}

.section-title {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-secondary);
  font-weight: 700;
  margin-bottom: 1rem;
  border-bottom: 1px solid var(--color-border);
  padding-bottom: 0.5rem;
}

.loading-state {
  text-align: center;
  color: var(--color-text-muted);
  padding: 1rem;
}

.inventory-content {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.stock-display {
  background-color: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  padding: 1rem;
  text-align: center;
}

.stock-label {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.stock-value {
  font-size: 2rem;
  font-weight: 700;
  color: #15803d;
}

.stock-value.low-stock {
  color: var(--color-text-error);
}

.error-message {
  background-color: #fef2f2;
  border: 1px solid #fecaca;
  color: var(--color-text-error);
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
}

.adjustment-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-dark-hover);
}

.form-select,
.form-input {
  width: 100%;
  padding: 0.625rem;
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  box-sizing: border-box;
  background-color: var(--color-white);
  height: 2.5rem;
}

.form-select {
  appearance: none;
  -webkit-appearance: none;
  -moz-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%23475569' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.625rem center;
  padding-right: 2rem;
  cursor: pointer;
}

.form-select:focus,
.form-input:focus {
  outline: 2px solid var(--color-primary);
  outline-offset: -1px;
}

.btn-adjust {
  padding: 0.75rem 1rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-adjust:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-add {
  background-color: var(--color-primary);
  color: var(--color-white);
}

.btn-add:hover:not(:disabled) {
  background-color: #1d4ed8;
}

.btn-remove {
  background-color: var(--color-text-error);
  color: var(--color-white);
}

.btn-remove:hover:not(:disabled) {
  opacity: 0.9;
}
</style>

