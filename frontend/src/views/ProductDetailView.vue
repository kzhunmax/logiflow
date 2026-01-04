<script setup>

import {useRoute, useRouter} from "vue-router";
import {useProductStore} from "@/stores/productStore.js";
import {ref} from "vue";
import InventoryManager from "@/components/InventoryManager.vue";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import TrashIcon from "@/components/icons/TrashIcon.vue";
import {useField, useForm} from "vee-validate";
import {productSchema} from "@/validation/productSchema.js";

const route = useRoute()
const router = useRouter()
const productStore = useProductStore()
const productId = route.params.id

productStore.fetchProductById(productId)

const isEditing = ref(false)

const { handleSubmit: validateAndSubmit, errors, resetForm, setValues } = useForm({
  validationSchema: productSchema
})

const { value: editName } = useField('name')
const { value: editSku } = useField('sku')
const { value: editPrice } = useField('price')
const attributes = ref([])

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

function enableEditMode() {
  setValues({
    name: productStore.product.name,
    sku: productStore.product.sku,
    price: productStore.product.price
  })

  attributes.value = transformAttributesToArray(productStore.product.attributes)
  if (attributes.value.length === 0) {
    attributes.value.push({ key: '', value: '' })
  }

  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
  resetForm()
}

function addAttributeRow() {
  attributes.value.push({key: '', value: ''});
}

function removeAttributeRow(index) {
  attributes.value.splice(index, 1);
}

const handleUpdate = validateAndSubmit(async (values) => {
  const payload = {
    name: values.name,
    sku: values.sku,
    price: parseFloat(values.price),
    attributes: transformArrayToObject(attributes.value)
  }
  await productStore.updateProduct(productId, payload)
  await productStore.fetchProductById(productId)
  isEditing.value = false
})

async function handleDelete() {
  if (confirm("Are you sure you want to delete this product?")) {
    await productStore.deleteProduct(productId)
    await router.push('/')
  }
}

</script>

<template>
  <main class="grow p-6 md:p-10 flex justify-center items-start">
    <div v-if="productStore.loading" class="text-center p-10">Loading...</div>
    <div v-else-if="productStore.error" class="text-red-500 p-10">{{ productStore.error }}</div>

    <div v-else-if="productStore.product"
         class="w-full max-w-4xl bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden relative min-h-125">
      <div class="p-6 px-8 border-b border-slate-200 flex justify-between items-start bg-slate-50">
        <div class="flex items-center gap-4">
          <div
            class="w-16 h-16 bg-white border border-slate-200 rounded-lg flex items-center justify-center text-slate-300 shadow-sm">
            <span>IMG</span>
          </div>
          <div>
            <div v-if="!isEditing" id="view-header">
              <h1 class="text-2xl font-bold flex items-center gap-3">
                {{ productStore.product.name }}
                <span
                  :class="productStore.product.active ? 'py-0.5 px-2.5 rounded-full bg-green-100 text-green-700 text-xs font-medium border border-green-200' : 'py-0.5 px-2.5 rounded-full bg-slate-100 text-slate-600 text-xs font-medium border border-slate-200'">{{
                    productStore.product.active ? 'In Stock' : 'Inactive'
                  }}</span>
              </h1>
              <p class="text-sm text-slate-500 mt-1 font-mono">SKU: {{
                  productStore.product.sku
                }}</p>
            </div>
            <div v-else id="edit-header">
              <h1 class="text-2xl font-bold">Edit Product</h1>
              <p class="text-sm text-slate-500 mt-1">Update product details and attributes.</p>
            </div>
          </div>
        </div>

        <div class="flex gap-2">
          <div v-if="!isEditing" id="view-actions" class="flex gap-2">
            <button @click="handleDelete"
                    class="py-2 px-4 bg-white border border-slate-200 text-slate-600 rounded-lg text-sm font-medium cursor-pointer transition-all hover:bg-slate-50 hover:text-red-700 hover:border-red-200">
              Delete
            </button>
            <button @click="enableEditMode"
                    class="py-2 px-4 bg-blue-600 text-white rounded-lg text-sm font-medium shadow-sm border-none cursor-pointer transition-all hover:bg-blue-700 active:scale-95">
              Edit Product
            </button>
          </div>
          <div v-else id="edit-actions" class="flex gap-2">
            <button @click="cancelEdit"
                    class="py-2 px-4 bg-slate-50 border border-slate-200 text-slate-600 rounded-lg text-sm font-medium cursor-pointer transition-all hover:bg-slate-200">
              Cancel
            </button>
            <button @click="handleUpdate"
                    class="py-2 px-4 bg-blue-600 text-white rounded-lg text-sm font-medium shadow-sm border-none cursor-pointer transition-all hover:bg-blue-700 active:scale-95">
              Save Changes
            </button>
          </div>
        </div>
      </div>

      <div class="p-8">
        <div v-if="!isEditing" id="view-mode" class="flex flex-col gap-8 animate-fade-in">
          <div>
            <h3
              class="text-xs uppercase tracking-wide text-slate-400 font-bold mb-4 border-b border-slate-200 pb-2">
              General Information</h3>
            <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
              <div>
                <p class="text-sm text-slate-500 mb-1">Base Price</p>
                <p class="text-xl font-semibold">${{ productStore.product.price }}</p>
              </div>
            </div>
          </div>

          <InventoryManager
            v-if="productStore.product?.sku"
            :sku="productStore.product.sku"
          />

          <div
            v-if="productStore.product.attributes && Object.keys(productStore.product.attributes).length > 0">
            <h3
              class="text-xs uppercase tracking-wide text-slate-400 font-bold mb-4 border-b border-slate-200 pb-2">
              Product Attributes</h3>
            <div class="bg-slate-50 rounded-lg border border-slate-200 overflow-hidden">
              <table class="w-full text-sm text-left border-collapse">
                <tbody>
                <tr v-for="(value, key) in productStore.product.attributes" :key="key"
                    class="border-b border-slate-200 last:border-b-0 transition-colors hover:bg-white">
                  <td
                    class="py-3 px-4 font-medium text-slate-600 w-1/3 border-r border-slate-200 bg-slate-50/50">
                    {{ key }}
                  </td>
                  <td class="py-3 px-4">{{ value }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div v-else id="edit-mode" class="flex flex-col gap-8 animate-fade-in">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium text-slate-600">Product Name</label>
              <input v-model="editName" type="text"
                     class="w-full py-2.5 px-3 border border-slate-300 rounded-lg text-sm focus:outline-2 focus:outline-blue-600 focus:-outline-offset-1"/>
              <span v-if="errors.name" class="text-red-500 text-xs">{{ errors.name }}</span>
            </div>
            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium text-slate-600">SKU</label>
              <input v-model="editSku" type="text"
                     class="w-full py-2.5 px-3 border border-slate-300 rounded-lg text-sm focus:outline-2 focus:outline-blue-600 focus:-outline-offset-1"/>
              <span v-if="errors.sku" class="text-red-500 text-xs">{{ errors.sku }}</span>
            </div>
            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium text-slate-600">Price ($)</label>
              <input v-model="editPrice" type="number" step="0.01"
                     class="w-full py-2.5 px-3 border border-slate-300 rounded-lg text-sm focus:outline-2 focus:outline-blue-600 focus:-outline-offset-1"/>
              <span v-if="errors.price" class="text-red-500 text-xs">{{ errors.price }}</span>
            </div>
          </div>

          <hr class="border-t border-slate-200">

          <div>
            <div class="flex justify-between items-center mb-4">
              <h3 class="text-xs uppercase tracking-wide text-slate-400 font-bold">Manage
                Attributes</h3>
              <button type="button" @click="addAttributeRow"
                      class="flex items-center gap-2 text-blue-600 text-xs font-bold py-1 px-2 rounded bg-transparent border-none cursor-pointer uppercase tracking-wide transition-colors hover:bg-violet-50">
                <PlusIcon class="w-3 h-3 fill-current"/>
                Add New
              </button>
            </div>

            <div id="attributes-container" class="flex flex-col gap-3">
              <div v-for="(attr, index) in attributes" :key="index"
                   class="flex gap-2 items-center">
                <input v-model="attr.key" type="text"
                       class="flex-1 py-2 px-3 border border-slate-200 rounded-md text-sm"
                       placeholder="Key"/>
                <span class="text-slate-200">:</span>
                <input v-model="attr.value" type="text"
                       class="flex-1 py-2 px-3 border border-slate-200 rounded-md text-sm"
                       placeholder="Value"/>
                <button type="button" @click="removeAttributeRow(index)"
                        class="w-8 h-8 p-2 flex items-center justify-center text-red-400 bg-transparent border-none rounded-full cursor-pointer transition-all hover:text-red-700 hover:bg-red-50">
                  <TrashIcon class="w-4 h-4 fill-current"/>
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
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.animate-fade-in {
  animation: fadeIn 0.3s ease;
}

</style>
