import {ref} from "vue";
import {defineStore} from "pinia";
import {productService} from "@/services/productService.js";

export const useProductStore = defineStore('product', () => {
  const products = ref([])
  const product = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const searchQuery = ref('')
  const pagination = ref({
    currentPage: 1,
    totalPages: 1,
    totalItems: 0,
    pageSize: 10
  })


  async function fetchProducts(page = 1, search = searchQuery.value) {
    loading.value = true
    error.value = null
    searchQuery.value = search

    try {
      const response = await productService.getAll(page - 1, pagination.value.pageSize, search)

      products.value = response.data.content
      pagination.value.totalPages = response.data.page.totalPages
      pagination.value.totalItems = response.data.page.totalElements
      pagination.value.currentPage = page
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to load products.'
    } finally {
      loading.value = false
    }
  }

  async function createProduct(productData) {
    loading.value = true
    error.value = null

    try {
      await productService.create(productData)
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function fetchProductById(productId) {
    loading.value = true
    error.value = null

    try {
      const response = await productService.getById(productId)
      product.value = response.data
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function updateProduct(productId, productData) {
    loading.value = true
    error.value = null

    try {
      await productService.update(productId, productData)
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function deleteProduct(productId) {
    loading.value = true
    error.value = null

    try {
      await productService.delete(productId)
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return {
    products,
    product,
    loading,
    error,
    searchQuery,
    pagination,
    fetchProducts,
    fetchProductById,
    createProduct,
    updateProduct,
    deleteProduct
  }
})
