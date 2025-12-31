<script setup>

import {ref} from "vue";

const props = defineProps({
  loading: {type: Boolean, default: false}
})
const emit = defineEmits(['product-submit'])
const name = ref('')
const sku = ref('')
const price = ref('')
const attributes = ref([
  {key: '', value: ''}
])

const errors = ref({
  name: '',
  sku: '',
  price: '',
  attributes: []
})

function clearErrors() {
  errors.value = {
    name: '',
    sku: '',
    price: '',
    attributes: []
  };
}

function isDataValidated() {
  clearErrors()
  let isValid = true

  if (!name.value || name.value.trim() === '') {
    errors.value.name = 'Product name must not be blank'
    isValid = false
  } else if (name.value.length < 2 || name.value.length > 100) {
    errors.value.name = 'Name must be between 2 and 100 characters long.'
    isValid = false
  }

  if (!sku.value || sku.value.trim() === '') {
    errors.value.sku = 'SKU must not be blank'
    isValid = false
  } else if (sku.value.length < 2 || sku.value.length > 150) {
    errors.value.sku = 'SKU must be between 2 and 150 characters long.'
    isValid = false
  }

  if (parseFloat(price.value) < 0.01) {
    errors.value.price = 'Price must be greater than 0.01'
    isValid = false
  } else if (!price.value) {
    errors.value.price = 'Price must not be blank'
    isValid = false
  }
  return isValid
}

function handleSubmit() {
  if (props.loading === true) return
  if (!isDataValidated()) return
  let normalizedAttributes = Object.fromEntries(
    attributes.value
      .filter(attr => attr.key.trim() !== '')
      .map(attr => [attr.key.trim(), attr.value.trim()])
  )
  const productData = {
    name: name.value.trim(),
    sku: sku.value.trim(),
    price: parseFloat(price.value),
    attributes: normalizedAttributes
  }
  emit('product-submit', productData);
}

</script>

<template>
  <main class="main-container">
    <div class="card">
      <div class="card-header">
        <div>
          <h1 class="card-title">Create Product</h1>
          <p class="card-subtitle">Add a new item to your global catalog.</p>
        </div>
        <button class="close-btn" @click="emit('cancel')">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512">
            <path
              d="M55.1 73.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L147.2 256 9.9 393.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192.5 301.3 329.9 438.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.8 256 375.1 118.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192.5 210.7 55.1 73.4z"/>
          </svg>
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="card-body" novalidate>
        <div class="section">
          <h3 class="section-title">Basic Information</h3>

          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">Product Name</label>
              <input
                @input="errors.name = null"
                v-model="name"
                type="text"
                placeholder="e.g. Wireless Gaming Mouse"
                class="form-input"
              />
              <span v-if="errors.name" class="error-text">{{ errors.name }}</span>
            </div>

            <div class="form-group">
              <label class="form-label">SKU Code</label>
              <div class="input-with-icon">
                <span class="input-icon"><svg xmlns="http://www.w3.org/2000/svg"
                                              viewBox="0 0 448 512"><path
                  d="M32 32C14.3 32 0 46.3 0 64L0 448c0 17.7 14.3 32 32 32s32-14.3 32-32L64 64c0-17.7-14.3-32-32-32zm88 0c-13.3 0-24 10.7-24 24l0 400c0 13.3 10.7 24 24 24s24-10.7 24-24l0-400c0-13.3-10.7-24-24-24zm72 32l0 384c0 17.7 14.3 32 32 32s32-14.3 32-32l0-384c0-17.7-14.3-32-32-32s-32 14.3-32 32zm208-8l0 400c0 13.3 10.7 24 24 24s24-10.7 24-24l0-400c0-13.3-10.7-24-24-24s-24 10.7-24 24zm-96 0l0 400c0 13.3 10.7 24 24 24s24-10.7 24-24l0-400c0-13.3-10.7-24-24-24s-24 10.7-24 24z"/></svg></span>
                <input
                  @input="errors.sku = null"
                  v-model="sku"
                  type="text"
                  placeholder="LOG-2024-001"
                  class="form-input with-icon font-mono"
                />
              </div>
              <span v-if="errors.sku" class="error-text">{{ errors.sku }}</span>
            </div>

            <div class="form-group">
              <label class="form-label">Base Price</label>
              <div class="input-with-icon">
                <span class="input-icon-currency">$</span>
                <input
                  @input="errors.price = null"
                  v-model="price"
                  type="number"
                  placeholder="0.00"
                  class="form-input with-currency"
                />
              </div>
              <span v-if="errors.price" class="error-text">{{ errors.price }}</span>
            </div>
          </div>
        </div>

        <hr class="divider">

        <div class="section">
          <div class="section-header">
            <div>
              <h3 class="section-title">Dynamic Attributes</h3>
              <p class="section-subtitle">Add custom properties like Color, Size, or Material.</p>
            </div>
            <button type="button" class="add-attr-btn"
                    @click="attributes.push({key: '', value: ''})">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512">
                <path
                  d="M256 64c0-17.7-14.3-32-32-32s-32 14.3-32 32l0 160-160 0c-17.7 0-32 14.3-32 32s14.3 32 32 32l160 0 0 160c0 17.7 14.3 32 32 32s32-14.3 32-32l0-160 160 0c17.7 0 32-14.3 32-32s-14.3-32-32-32l-160 0 0-160z"/>
              </svg>
              Add Attribute
            </button>
          </div>

          <div class="attributes-container">
            <div v-for="(attr, index) in attributes" :key="index" class="attribute-row">
              <div class="attribute-input-wrapper">
                <input v-model="attr.key" type="text" placeholder="Key" class="attribute-input"/>
              </div>
              <div class="attribute-arrow">→</div>
              <div class="attribute-input-wrapper">
                <input v-model="attr.value" type="text" placeholder="Value"
                       class="attribute-input"/>
              </div>
              <button
                type="button"
                class="remove-attr-btn"
                :class="{ disabled: attributes.length === 1 }"
                :disabled="attributes.length === 1"
                @click="attributes.splice(index, 1)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 640">
                  <path
                    d="M232.7 69.9L224 96L128 96C110.3 96 96 110.3 96 128C96 145.7 110.3 160 128 160L512 160C529.7 160 544 145.7 544 128C544 110.3 529.7 96 512 96L416 96L407.3 69.9C402.9 56.8 390.7 48 376.9 48L263.1 48C249.3 48 237.1 56.8 232.7 69.9zM512 208L128 208L149.1 531.1C150.7 556.4 171.7 576 197 576L443 576C468.3 576 489.3 556.4 490.9 531.1L512 208z"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="card-footer">
          <button type="button" class="cancel-btn" @click="emit('cancel')">Cancel</button>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? 'Creating...' : 'Create Product' }}
          </button>
        </div>
      </form>
    </div>
  </main>
</template>

<style scoped>
.main-container {
  flex-grow: 1;
  padding: 1.5rem;
  display: flex;
  justify-content: center;
}

@media (min-width: 768px) {
  .main-container {
    padding: 2.5rem;
  }
}

.card {
  width: 100%;
  max-width: 48rem;
  background: var(--color-white);
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.card-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: var(--color-background);
}

.card-title {
  font-size: 1.5rem;
  font-weight: 700;
}

.card-subtitle {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.close-btn {
  color: var(--color-text-muted);
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.25rem;
  line-height: 1;
}

.close-btn:hover {
  color: var(--color-dark-hover);
}

.close-btn svg {
  width: 1rem;
  height: 1rem;
  fill: currentColor;
}

.card-body {
  padding: 2rem;
}

.section {
  margin-bottom: 2rem;
}

.section-title {
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text-secondary);
  font-weight: 700;
  margin-bottom: 1rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 1rem;
}

.section-subtitle {
  font-size: 0.75rem;
  color: var(--color-text-secondary);
  margin-top: 0.25rem;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1.5rem;
}

@media (min-width: 768px) {
  .form-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-label {
  font-size: 0.875rem;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  transition: border-color 0.15s;
}

.form-input::placeholder {
  color: var(--color-text-muted);
}

.form-input:hover {
  border-color: var(--color-text-secondary);
}

.form-input:focus {
  outline: none;
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
}

.input-with-icon {
  position: relative;
}

.input-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-muted);
}

.input-icon svg {
  width: 1rem;
  height: 1rem;
  fill: currentColor;
}

.input-icon-currency {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-text-muted);
  font-weight: 600;
}

.form-input.with-icon {
  padding-left: 2.5rem;
}

.form-input.with-currency {
  padding-left: 1.75rem;
}

.font-mono {
  font-family: ui-monospace, SFMono-Regular, monospace;
}

.error-text {
  font-size: 0.75rem;
  color: #dc2626;
}

.divider {
  border: none;
  border-top: 1px solid var(--color-border);
  margin: 2rem 0;
}

.add-attr-btn {
  color: var(--color-primary);
  background: none;
  border: 1px solid transparent;
  padding: 0.5rem 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.add-attr-btn:hover {
  background: rgba(37, 99, 235, 0.05);
  border-color: rgba(37, 99, 235, 0.1);
}

.add-attr-btn svg {
  width: 0.75rem;
  height: 0.75rem;
  fill: currentColor;
}

.attributes-container {
  background: var(--color-background);
  border-radius: 0.5rem;
  border: 1px solid var(--color-border);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.attribute-row {
  display: flex;
  gap: 0.75rem;
  align-items: center;
}

.attribute-row:hover .remove-attr-btn:not(.disabled) {
  opacity: 1;
}

.attribute-input-wrapper {
  flex: 1;
}

.attribute-input {
  width: 100%;
  padding: 0.625rem;
  border: 1px solid var(--color-border);
  border-radius: 0.375rem;
  font-size: 0.875rem;
}

.attribute-input:focus {
  outline: none;
  border-color: var(--color-primary);
}

.attribute-arrow {
  color: var(--color-text-muted);
  font-size: 1.125rem;
}

.remove-attr-btn {
  width: 2rem;
  height: 2rem;
  padding: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #f87171;
  background: none;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  opacity: 0;
  transition: all 0.15s;
}

.remove-attr-btn:hover {
  color: var(--color-text-error);
  background: rgba(254, 202, 202, 0.5);
}

.remove-attr-btn.disabled {
  color: #cbd5e1;
  cursor: not-allowed;
  opacity: 1;
}

.remove-attr-btn svg {
  width: 1rem;
  height: 1rem;
  fill: currentColor;
}

.card-footer {
  padding: 1.25rem 2rem;
  background: var(--color-background);
  border-top: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.75rem;
  margin: 2rem -2rem -2rem;
}

.cancel-btn {
  padding: 0.625rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-text-muted);
  background: none;
  border: none;
  border-radius: 0.5rem;
  cursor: pointer;
  transition: all 0.15s;
}

.cancel-btn:hover {
  color: var(--color-text);
  background: var(--color-border);
}

.submit-btn {
  padding: 0.625rem 1.25rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-white);
  background: var(--color-primary);
  border: none;
  border-radius: 0.5rem;
  box-shadow: 0 1px 2px rgba(37, 99, 235, 0.3);
  cursor: pointer;
  transition: all 0.15s;
}

.submit-btn:hover {
  background: #1d4ed8;
}

.submit-btn:active {
  transform: scale(0.95);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
