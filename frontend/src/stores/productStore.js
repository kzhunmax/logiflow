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

  async function fetchProducts(page = 1, search = searchQuery.value) {
    loading.value = true
    error.value = null
    searchQuery.value = search

    try {
      const response = await api.get('/catalog/products', {
        params: {page: page - 1, size: pageSize.value, search: search || undefined}
      })
      products.value = response.data.content || response.data
      totalPages.value = response.data.totalPages || 1
      totalItems.value = response.data.totalElements || products.value.length
      currentPage.value = page
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }
  return { products, loading, error, currentPage, totalPages, totalItems, searchQuery , fetchProducts }
})
