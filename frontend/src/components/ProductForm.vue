<script setup>

import {ref} from "vue";
import CloseIcon from "@/components/icons/CloseIcon.vue";
import BarcodeIcon from "@/components/icons/BarcodeIcon.vue";
import PlusIcon from "@/components/icons/PlusIcon.vue";
import TrashIcon from "@/components/icons/TrashIcon.vue";
import {useField, useForm} from "vee-validate";
import {productSchema} from "@/validation/productSchema.js";

const props = defineProps({
  loading: {type: Boolean, default: false}
})
const emit = defineEmits(['product-submit', 'cancel'])

const {handleSubmit, errors} = useForm({
  validationSchema: productSchema,
  initialValues: {
    name: '',
    sku: '',
    price: ''
  }
})

const {value: name} = useField('name')
const {value: sku} = useField('sku')
const {value: price} = useField('price')
const attributes = ref([{key: '', value: ''}])

const onSubmit = handleSubmit((values) => {
  if (props.loading) return

  const normalizedAttributes = Object.fromEntries(
    attributes.value
      .filter(attr => attr.key.trim() !== '')
      .map(attr => [attr.key.trim(), attr.value.trim()])
  )

  emit('product-submit', {
    name: values.name.trim(),
    sku: values.sku.trim(),
    price: parseFloat(values.price),
    attributes: normalizedAttributes
  })
})

</script>

<template>
  <main class="grow p-6 md:p-10 flex justify-center">
    <div
      class="w-full max-w-3xl bg-white rounded-xl shadow-md border border-slate-200 overflow-hidden">
      <div class="p-6 px-8 border-b border-slate-200 flex justify-between items-center bg-slate-50">
        <div>
          <h1 class="text-2xl font-bold">Create Product</h1>
          <p class="text-sm text-slate-500 mt-1">Add a new item to your global catalog.</p>
        </div>
        <button
          class="text-slate-500 bg-transparent border-none text-2xl cursor-pointer p-1 leading-none hover:text-slate-600"
          @click="emit('cancel')">
          <CloseIcon class="w-4 h-4 fill-current"/>
        </button>
      </div>

      <form @submit.prevent="onSubmit" class="p-8" novalidate>
        <div class="mb-8">
          <h3 class="text-xs uppercase tracking-wide text-slate-400 font-bold mb-4">Basic
            Information</h3>

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
                <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500">
                  <BarcodeIcon class="w-4 h-4 fill-current"/>
                </span>
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
              <h3 class="text-xs uppercase tracking-wide text-slate-400 font-bold">Dynamic
                Attributes</h3>
              <p class="text-xs text-slate-400 mt-1">Add custom properties like Color, Size, or
                Material.</p>
            </div>
            <button type="button"
                    class="text-blue-600 bg-transparent border border-transparent py-2 px-3 rounded-lg text-sm font-semibold cursor-pointer transition-all flex items-center gap-1 hover:bg-blue-600/5 hover:border-blue-600/10"
                    @click="attributes.push({key: '', value: ''})">
              <PlusIcon class="w-3 h-3 fill-current"/>
              Add Attribute
            </button>
          </div>

          <div class="bg-slate-50 rounded-lg border border-slate-200 p-4 flex flex-col gap-3">
            <div v-for="(attr, index) in attributes" :key="index"
                 class="flex gap-3 items-center group">
              <div class="flex-1">
                <input v-model="attr.key" type="text" placeholder="Key"
                       class="w-full py-2.5 px-3 border border-slate-200 rounded-md text-sm focus:outline-none focus:border-blue-600"/>
              </div>
              <div class="text-slate-500 text-lg">→</div>
              <div class="flex-1">
                <input v-model="attr.value" type="text" placeholder="Value"
                       class="w-full py-2.5 px-3 border border-slate-200 rounded-md text-sm focus:outline-none focus:border-blue-600"/>
              </div>
              <button
                type="button"
                class="w-8 h-8 p-2 flex items-center justify-center text-red-400 bg-transparent border-none rounded-full cursor-pointer opacity-0 group-hover:opacity-100 transition-all hover:text-red-700 hover:bg-red-200/50 disabled:text-slate-300 disabled:cursor-not-allowed disabled:opacity-100"
                :disabled="attributes.length === 1"
                @click="attributes.splice(index, 1)"
              >
                <TrashIcon class="w-4 h-4 fill-current"/>
              </button>
            </div>
          </div>
        </div>

        <div
          class="py-5 px-8 bg-slate-50 border-t border-slate-200 flex items-center justify-end gap-3 -mx-8 -mb-8 mt-8">
          <button type="button"
                  class="py-2.5 px-5 text-sm font-medium text-slate-500 bg-transparent border-none rounded-lg cursor-pointer transition-all hover:text-slate-900 hover:bg-slate-200"
                  @click="emit('cancel')">Cancel
          </button>
          <button type="submit"
                  class="py-2.5 px-5 text-sm font-medium text-white bg-blue-600 border-none rounded-lg shadow-sm cursor-pointer transition-all hover:bg-blue-700 active:scale-95 disabled:opacity-60 disabled:cursor-not-allowed"
                  :disabled="loading">
            {{ loading ? 'Creating...' : 'Create Product' }}
          </button>
        </div>
      </form>
    </div>
  </main>
</template>

<style scoped>

</style>
