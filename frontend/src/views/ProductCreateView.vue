<script setup>

import {useRouter} from "vue-router";
import {useProductStore} from "@/stores/productStore.js";
import ProductForm from "@/components/ProductForm.vue";

const router = useRouter()
const productStore = useProductStore()

async function handleSubmit(product) {
  try {
    await productStore.createProduct(product)
    await productStore.fetchProducts()
    await router.push('/')
  } catch (error) {
    console.error('Failed to create product:', error)
  }
}

</script>

<template>
  <ProductForm @product-submit="handleSubmit" @cancel="router.push('/')" :loading="productStore.loading"/>
</template>

<style scoped>

</style>
