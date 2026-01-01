import {ref} from "vue";
import api from "@/services/api.js";
import {defineStore} from "pinia";

export const useProductStore = defineStore('product', () => {
  const products = ref([])
  const loading = ref(false)
  const error = ref(null)
  const currentPage = ref(1)
  const totalPages = ref(1)
  const totalItems = ref(0)
  const pageSize = ref(10)
  const searchQuery = ref('')
  const product = ref(null)

  async function fetchProducts(page = 1, search = searchQuery.value) {
    loading.value = true
    error.value = null
    searchQuery.value = search

    try {
      const response = await api.get('/catalog/products', {
        params: {page: page - 1, size: pageSize.value, search: search || undefined}
      })

      products.value = response.data.content
      totalPages.value = response.data.page.totalPages
      totalItems.value = response.data.page.totalElements
      currentPage.value = page
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
      await api.post(`/catalog/products`, productData)
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
      const response = await api.get(`/catalog/products/${productId}`)
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
      await api.put(`/catalog/products/${productId}`, productData)
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
      await api.delete(`/catalog/products/${productId}`)
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  return { products, loading, error, currentPage, totalPages, totalItems, searchQuery, product, fetchProducts, createProduct, fetchProductById, updateProduct, deleteProduct}
})
