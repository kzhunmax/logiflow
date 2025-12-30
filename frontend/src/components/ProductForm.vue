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

let errors = ref({
  name: null,
  sku: null,
  price: null,
  attributes: []
})

function clearErrors() {
  errors.value = {
    name: null,
    sku: null,
    price: null,
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

  if (!price.value || isNaN(parseFloat(price.value))) {
    errors.value.price = 'Price must not be blank'
    isValid = false
  } else if (parseFloat(price.value) < 0.01) {
    errors.value.price = 'Price must be greater than 0.01'
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
  <form @submit.prevent="handleSubmit">
    <label for="name">Name:</label><br>
    <input @input="errors.name = null" v-model="name" type="text" id="name" name="name"><br>
    <span v-if="errors.name">{{ errors.name }}</span><br>
    <label for="sku">SKU:</label><br>
    <input @input="errors.sku = null" v-model="sku" type="text" id="sku" name="sku"><br>
    <span v-if="errors.sku">{{ errors.sku }}</span><br>
    <label for="price">Price:</label><br>
    <input @input="errors.price = null" v-model="price" type="number" min="0" step="0.01" id="price"
           name="price"><br>
    <span v-if="errors.price">{{ errors.price }}</span><br>
    <button type="button" @click="attributes.push({key: '', value: ''})">Add Attribute</button>
    <div v-for="(attr, index) in attributes" :key="index">
      <input v-model="attr.key" type="text" placeholder="Attribute Key">
      <input v-model="attr.value" type="text" placeholder="Attribute Value">
      <button type="button" @click="attributes.splice(index, 1)">Remove</button>
    </div>
    <br>
    <button :disabled="loading" type="submit" class="clickable">{{loading ? 'Loading...' : 'Submit'}}</button>
  </form>
</template>

<style scoped>

</style>
