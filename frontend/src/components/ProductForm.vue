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
  <main class="grow p-6 md:p-10 flex justify-center">
    <div class="w-full max-w-3xl bg-white rounded-xl shadow-md border border-slate-200 overflow-hidden">
      <div class="p-6 px-8 border-b border-slate-200 flex justify-between items-center bg-slate-50">
        <div>
          <h1 class="text-2xl font-bold">Create Product</h1>
          <p class="text-sm text-slate-500 mt-1">Add a new item to your global catalog.</p>
        </div>
        <button class="text-slate-500 bg-transparent border-none text-2xl cursor-pointer p-1 leading-none hover:text-slate-600" @click="emit('cancel')">
          <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512" class="w-4 h-4 fill-current">
            <path
              d="M55.1 73.4c-12.5-12.5-32.8-12.5-45.3 0s-12.5 32.8 0 45.3L147.2 256 9.9 393.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0L192.5 301.3 329.9 438.6c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L237.8 256 375.1 118.6c12.5-12.5 12.5-32.8 0-45.3s-32.8-12.5-45.3 0L192.5 210.7 55.1 73.4z"/>
          </svg>
        </button>
      </div>

      <form @submit.prevent="handleSubmit" class="p-8" novalidate>
        <div class="mb-8">
          <h3 class="text-xs uppercase tracking-wide text-slate-400 font-bold mb-4">Basic Information</h3>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium">Product Name</label>
              <input
                @input="errors.name = null"
                v-model="name"
                type="text"
                placeholder="e.g. Wireless Gaming Mouse"
                class="w-full py-3 px-3 border border-slate-200 rounded-lg text-sm transition-colors placeholder:text-slate-500 hover:border-slate-400 focus:outline-none focus:border-blue-600 focus:ring-3 focus:ring-blue-600/10"
              />
              <span v-if="errors.name" class="text-xs text-red-600">{{ errors.name }}</span>
            </div>

            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium">SKU Code</label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500"><svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="w-4 h-4 fill-current"><path d="M32 32C14.3 32 0 46.3 0 64L0 448c0 17.7 14.3 32 32 32s32-14.3 32-32L64 64c0-17.7-14.3-32-32-32zm88 0c-13.3 0-24 10.7-24 24l0 400c0 13.3 10.7 24 24 24s24-10.7 24-24l0-400c0-13.3-10.7-24-24-24zm72 32l0 384c0 17.7 14.3 32 32 32s32-14.3 32-32l0-384c0-17.7-14.3-32-32-32s-32 14.3-32 32zm208-8l0 400c0 13.3 10.7 24 24 24s24-10.7 24-24l0-400c0-13.3-10.7-24-24-24s-24 10.7-24 24zm-96 0l0 400c0 13.3 10.7 24 24 24s24-10.7 24-24l0-400c0-13.3-10.7-24-24-24s-24 10.7-24 24z"/></svg></span>
                <input
                  @input="errors.sku = null"
                  v-model="sku"
                  type="text"
                  placeholder="LOG-2024-001"
                  class="w-full py-3 pl-10 pr-3 border border-slate-200 rounded-lg text-sm font-mono transition-colors placeholder:text-slate-500 hover:border-slate-400 focus:outline-none focus:border-blue-600 focus:ring-3 focus:ring-blue-600/10"
                />
              </div>
              <span v-if="errors.sku" class="text-xs text-red-600">{{ errors.sku }}</span>
            </div>

            <div class="flex flex-col gap-2">
              <label class="text-sm font-medium">Base Price</label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500 font-semibold">$</span>
                <input
                  @input="errors.price = null"
                  v-model="price"
                  type="number"
                  placeholder="0.00"
                  class="w-full py-3 pl-7 pr-3 border border-slate-200 rounded-lg text-sm transition-colors placeholder:text-slate-500 hover:border-slate-400 focus:outline-none focus:border-blue-600 focus:ring-3 focus:ring-blue-600/10"
                />
              </div>
              <span v-if="errors.price" class="text-xs text-red-600">{{ errors.price }}</span>
            </div>
          </div>
        </div>

        <hr class="border-t border-slate-200 my-8">

        <div class="mb-8">
          <div class="flex justify-between items-end mb-4">
            <div>
              <h3 class="text-xs uppercase tracking-wide text-slate-400 font-bold">Dynamic Attributes</h3>
              <p class="text-xs text-slate-400 mt-1">Add custom properties like Color, Size, or Material.</p>
            </div>
            <button type="button" class="text-blue-600 bg-transparent border border-transparent py-2 px-3 rounded-lg text-sm font-semibold cursor-pointer transition-all flex items-center gap-1 hover:bg-blue-600/5 hover:border-blue-600/10"
                    @click="attributes.push({key: '', value: ''})">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" class="w-3 h-3 fill-current">
                <path
                  d="M256 64c0-17.7-14.3-32-32-32s-32 14.3-32 32l0 160-160 0c-17.7 0-32 14.3-32 32s14.3 32 32 32l160 0 0 160c0 17.7 14.3 32 32 32s32-14.3 32-32l0-160 160 0c17.7 0 32-14.3 32-32s-14.3-32-32-32l-160 0 0-160z"/>
              </svg>
              Add Attribute
            </button>
          </div>

          <div class="bg-slate-50 rounded-lg border border-slate-200 p-4 flex flex-col gap-3">
            <div v-for="(attr, index) in attributes" :key="index" class="flex gap-3 items-center group">
              <div class="flex-1">
                <input v-model="attr.key" type="text" placeholder="Key" class="w-full py-2.5 px-3 border border-slate-200 rounded-md text-sm focus:outline-none focus:border-blue-600"/>
              </div>
              <div class="text-slate-500 text-lg">→</div>
              <div class="flex-1">
                <input v-model="attr.value" type="text" placeholder="Value" class="w-full py-2.5 px-3 border border-slate-200 rounded-md text-sm focus:outline-none focus:border-blue-600"/>
              </div>
              <button
                type="button"
                class="w-8 h-8 p-2 flex items-center justify-center text-red-400 bg-transparent border-none rounded-full cursor-pointer opacity-0 group-hover:opacity-100 transition-all hover:text-red-700 hover:bg-red-200/50 disabled:text-slate-300 disabled:cursor-not-allowed disabled:opacity-100"
                :disabled="attributes.length === 1"
                @click="attributes.splice(index, 1)"
              >
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 640" class="w-4 h-4 fill-current">
                  <path
                    d="M232.7 69.9L224 96L128 96C110.3 96 96 110.3 96 128C96 145.7 110.3 160 128 160L512 160C529.7 160 544 145.7 544 128C544 110.3 529.7 96 512 96L416 96L407.3 69.9C402.9 56.8 390.7 48 376.9 48L263.1 48C249.3 48 237.1 56.8 232.7 69.9zM512 208L128 208L149.1 531.1C150.7 556.4 171.7 576 197 576L443 576C468.3 576 489.3 556.4 490.9 531.1L512 208z"/>
                </svg>
              </button>
            </div>
          </div>
        </div>

        <div class="py-5 px-8 bg-slate-50 border-t border-slate-200 flex items-center justify-end gap-3 -mx-8 -mb-8 mt-8">
          <button type="button" class="py-2.5 px-5 text-sm font-medium text-slate-500 bg-transparent border-none rounded-lg cursor-pointer transition-all hover:text-slate-900 hover:bg-slate-200" @click="emit('cancel')">Cancel</button>
          <button type="submit" class="py-2.5 px-5 text-sm font-medium text-white bg-blue-600 border-none rounded-lg shadow-sm cursor-pointer transition-all hover:bg-blue-700 active:scale-95 disabled:opacity-60 disabled:cursor-not-allowed" :disabled="loading">
            {{ loading ? 'Creating...' : 'Create Product' }}
          </button>
        </div>
      </form>
    </div>
  </main>
</template>

<style scoped>

</style>
