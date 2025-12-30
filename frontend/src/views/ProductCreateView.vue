<script setup>

import {useRouter} from "vue-router";
import {useProductStore} from "@/stores/productStore.js";
import ProductForm from "@/components/ProductForm.vue";

const router = useRouter()
const productStore = useProductStore()

function handleSubmit(product) {
  productStore.createProduct(product).then(() => {
    router.push('/')
  }).catch((error) => {
    console.error("Error creating product:", error)
  })
}

</script>

<template>
  <ProductForm @product-submit="handleSubmit" :loading="productStore.loading"/>
</template>

<style scoped>

</style>
