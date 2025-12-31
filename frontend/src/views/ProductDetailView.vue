<script setup>

import {useRoute, useRouter} from "vue-router";
import {useProductStore} from "@/stores/productStore.js";
import {onMounted, ref} from "vue";

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const productId = route.params.id

const isEditing = ref(false)
const editingForm = ref({
  name: '',
  sku: '',
  price: 0,
  attributes: []
})

function transformAttributesToArray(attributes) {
  if (!attributes) return [];
  return Object.entries(attributes).map(([key, value]) => ({key, value}));
}

function transformArrayToObject(attributesArray) {
  const attrs = {};
  attributesArray.forEach(({key, value}) => {
    attrs[key] = value;
  });
  return attrs;
}

onMounted(() => {
  productStore.fetchProductById(productId)
})

function enableEditMode() {
  editingForm.value = {
    name: productStore.product.name,
    sku: productStore.product.sku,
    price: productStore.product.price,
    attributes: transformAttributesToArray(productStore.product.attributes)
  }

  if (editingForm.value.attributes.length === 0) {
    editingForm.value.attributes.push({key: '', value: ''})
  }

  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
  editingForm.value = {name: '', sku: '', price: 0, attributes: []}
}

function addAttributeRow() {
  editingForm.value.attributes.push({key: '', value: ''});
}

function removeAttributeRow(index) {
  editingForm.value.attributes.splice(index, 1);
}

async function handleUpdate() {
  const payload = {
    name: editingForm.value.name,
    sku: editingForm.value.sku,
    price: parseFloat(editingForm.value.price),
    attributes: transformArrayToObject(editingForm.value.attributes)
  }
  await productStore.updateProduct(productId, payload)
  await productStore.fetchProductById(productId)
  isEditing.value = false
}

async function handleDelete() {
  if (confirm("Are you sure you want to delete this product?")) {
    await productStore.deleteProduct(productId)
    await router.push('/')
  }
}

</script>

<template>
  <main>
    <div v-if="productStore.loading" class="text-center p-10">Loading...</div>
    <div v-else-if="productStore.error" class="text-red-500 p-10">{{ productStore.error }}</div>

    <div v-else-if="productStore.product" class="product-card">
      <div class="card-header">
        <div class="header-left">
          <div class="product-image-placeholder">
            <span>IMG</span>
          </div>
          <div>
            <div v-if="!isEditing" id="view-header">
              <h1 class="product-title">
                {{ productStore.product.name }}
                <span
                  :class="productStore.product.active ? 'status-badge-active' : 'status-badge-inactive'">{{ productStore.product.active ? 'In Stock' : 'Inactive' }}</span>
              </h1>
              <p class="sku-text">SKU: {{ productStore.product.sku }}</p>
            </div>
            <div v-else id="edit-header">
              <h1 class="edit-title">Edit Product</h1>
              <p class="edit-subtitle">Update product details and attributes.</p>
            </div>
          </div>
        </div>

        <div class="actions-container">
          <div v-if="!isEditing" id="view-actions" class="actions-container">
            <button @click="handleDelete" class="btn-delete">Delete</button>
            <button @click="enableEditMode" class="btn-primary">Edit Product</button>
          </div>
          <div v-else id="edit-actions" class="actions-container">
            <button @click="cancelEdit" class="btn-cancel">Cancel</button>
            <button @click="handleUpdate" class="btn-primary">Save Changes</button>
          </div>
        </div>
      </div>

      <div class="card-body">
        <div v-if="!isEditing" id="view-mode" class="content-section section-spacing">
          <div>
            <h3 class="section-title">General Information</h3>
            <div class="info-grid">
              <div>
                <p class="info-label">Base Price</p>
                <p class="info-value">${{ productStore.product.price }}</p>
              </div>
            </div>
          </div>

          <div v-if="productStore.product.attributes">
            <h3 class="section-title">Product Attributes</h3>
            <div class="attributes-table-container">
              <table class="attributes-table">
                <tbody>
                <tr v-for="(value, key) in productStore.product.attributes" :key="key">
                  <td class="attr-key">{{ key }}</td>
                  <td class="attr-value">{{ value }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div v-else id="edit-mode" class="content-section section-spacing">
          <div class="form-grid">
            <div class="form-group">
              <label class="form-label">Product Name</label>
              <input v-model="editingForm.name" type="text" class="form-input"/>
            </div>
            <div class="form-group">
              <label class="form-label">SKU</label>
              <input v-model="editingForm.sku" type="text" class="form-input form-input"/>
            </div>
            <div class="form-group">
              <label class="form-label">Price ($)</label>
              <input v-model="editingForm.price" type="number" step="0.01" class="form-input"/>
            </div>
          </div>

          <hr class="divider">

          <div>
            <div class="attributes-header">
              <h3 class="section-title"
                  style="margin-bottom: 0; border-bottom: none; padding-bottom: 0;">Manage
                Attributes</h3>
              <button type="button" @click="addAttributeRow" class="btn-add-attribute">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512">
                  <path
                    d="M256 64c0-17.7-14.3-32-32-32s-32 14.3-32 32l0 160-160 0c-17.7 0-32 14.3-32 32s14.3 32 32 32l160 0 0 160c0 17.7 14.3 32 32 32s32-14.3 32-32l0-160 160 0c17.7 0 32-14.3 32-32s-14.3-32-32-32l-160 0 0-160z"/>
                </svg>
                Add New
              </button>
            </div>

            <div id="attributes-container" class="attributes-list">
              <div v-for="(attr, index) in editingForm.attributes" :key="index"
                   class="attribute-row">
                <input v-model="attr.key" type="text" class="attr-input" placeholder="Key"/>
                <span class="attr-separator">:</span>
                <input v-model="attr.value" type="text" class="attr-input" placeholder="Value"/>
                <button type="button" @click="removeAttributeRow(index)" class="btn-remove-attr">
                  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 640">
                    <path
                      d="M232.7 69.9L224 96L128 96C110.3 96 96 110.3 96 128C96 145.7 110.3 160 128 160L512 160C529.7 160 544 145.7 544 128C544 110.3 529.7 96 512 96L416 96L407.3 69.9C402.9 56.8 390.7 48 376.9 48L263.1 48C249.3 48 237.1 56.8 232.7 69.9zM512 208L128 208L149.1 531.1C150.7 556.4 171.7 576 197 576L443 576C468.3 576 489.3 556.4 490.9 531.1L512 208z"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </main>
</template>


<style scoped>
main {
  flex-grow: 1;
  padding: 1.5rem;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

@media (min-width: 768px) {
  main {
    padding: 2.5rem;
  }
}

.text-center {
  text-align: center;
}

.p-10 {
  padding: 2.5rem;
}

.text-red-500 {
  color: #ef4444;
}

.product-card {
  width: 100%;
  max-width: 56rem;
  background-color: var(--color-white);
  border-radius: 0.75rem;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  border: 1px solid var(--color-border);
  overflow: hidden;
  position: relative;
  min-height: 500px;
}

.card-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  background-color: var(--color-background);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.product-image-placeholder {
  width: 4rem;
  height: 4rem;
  background-color: var(--color-white);
  border: 1px solid var(--color-border);
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #cbd5e1;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
}

.product-title {
  font-size: 1.5rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.status-badge-active {
  padding: 0.125rem 0.625rem;
  border-radius: 9999px;
  background-color: #dcfce7;
  color: #15803d;
  font-size: 0.75rem;
  font-weight: 500;
  border: 1px solid #bbf7d0;
}

.status-badge-inactive {
  padding: 0.125rem 0.625rem;
  border-radius: 9999px;
  background-color: #f1f5f9;
  color: #475569;
  font-size: 0.75rem;
  font-weight: 500;
  border: 1px solid var(--color-border);
}

.sku-text {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
  font-family: monospace;
}

.edit-title {
  font-size: 1.5rem;
  font-weight: 700;
}

.edit-subtitle {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-top: 0.25rem;
}

.actions-container {
  display: flex;
  gap: 0.5rem;
}

.btn-delete {
  padding: 0.5rem 1rem;
  background-color: var(--color-white);
  border: 1px solid var(--color-border);
  color: var(--color-dark-hover);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-delete:hover {
  background-color: var(--color-background);
  color: var(--color-text-error);
  border-color: #fecaca;
}

.btn-primary {
  padding: 0.5rem 1rem;
  background-color: var(--color-primary);
  color: var(--color-white);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  box-shadow: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
  border: none;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-primary:hover {
  background-color: #1d4ed8;
}

.btn-primary:active {
  transform: scale(0.95);
}

.btn-cancel {
  padding: 0.5rem 1rem;
  background-color: var(--color-background);
  border: 1px solid var(--color-border);
  color: var(--color-dark-hover);
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.15s;
}

.btn-cancel:hover {
  background-color: var(--color-border);
}

.card-body {
  padding: 2rem;
}

.content-section {
  animation: fadeIn 0.3s ease;
}

.section-spacing {
  display: flex;
  flex-direction: column;
  gap: 2rem;
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

.info-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2rem;
}

@media (min-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.info-label {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}

.info-value {
  font-size: 1.25rem;
  font-weight: 600;
}

.attributes-table-container {
  background-color: var(--color-background);
  border-radius: 0.5rem;
  border: 1px solid var(--color-border);
  overflow: hidden;
}

.attributes-table {
  width: 100%;
  font-size: 0.875rem;
  text-align: left;
  border-collapse: collapse;
}

.attributes-table tbody tr {
  border-bottom: 1px solid var(--color-border);
  transition: background-color 0.15s;
}

.attributes-table tbody tr:last-child {
  border-bottom: none;
}

.attributes-table tbody tr:hover {
  background-color: var(--color-white);
}

.attr-key {
  padding: 0.75rem 1rem;
  font-weight: 500;
  color: var(--color-dark-hover);
  width: 33.333%;
  border-right: 1px solid var(--color-border);
  background-color: rgba(248, 250, 252, 0.5);
}

.attr-value {
  padding: 0.75rem 1rem;
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
  color: var(--color-dark-hover);
}

.form-input {
  width: 100%;
  padding: 0.625rem;
  border: 1px solid #cbd5e1;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  box-sizing: border-box;
}

.form-input:focus {
  outline: 2px solid var(--color-primary);
  outline-offset: -1px;
}

.divider {
  border: none;
  border-top: 1px solid var(--color-border);
}

.attributes-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.btn-add-attribute {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--color-primary);
  font-size: 0.75rem;
  font-weight: 700;
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  background: none;
  border: none;
  cursor: pointer;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  transition: background-color 0.15s;
}

.btn-add-attribute:hover {
  background-color: #f5f3ff;
}

.btn-add-attribute svg {
  width: 0.75rem;
  height: 0.75rem;
  fill: currentColor;
}

.attributes-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.attribute-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.attr-input {
  flex: 1;
  padding: 0.5rem;
  border: 1px solid var(--color-border);
  border-radius: 0.375rem;
  font-size: 0.875rem;
}

.attr-separator {
  color: var(--color-border);
}

.btn-remove-attr {
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
  transition: all 0.15s;
}

.btn-remove-attr:hover {
  color: var(--color-text-error);
  background-color: #fef2f2;
}

.btn-remove-attr svg {
  width: 1rem;
  height: 1rem;
  fill: currentColor;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.fade-in {
  animation: fadeIn 0.3s ease;
}

</style>
