import {defineStore} from "pinia";
import {ref} from "vue";
import api from "@/services/api.js";

export const useInventoryStore = defineStore('inventory', () => {
  const stock = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const pagination = ref({
    currentPage: 1,
    totalPages: 1,
    totalItems: 0,
    pageSize: 10
  })
  const inventoryList = ref([])

  async function fetchStock(sku) {
    loading.value = true
    error.value = null

    try {
      const response = await api.get(`/inventory/${sku}`)
      stock.value = response.data
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function adjustStock(payload) {
    loading.value = true
    error.value = null

    try {
      await api.post('/inventory/stock', payload)
      await fetchStock(payload.sku)
      await fetchAllInventory(pagination.value.currentPage);
    } catch (err) {
      if (err.response?.status === 409) {
        error.value = 'Cannot remove more stock than available'
      } else {
        error.value = err.response?.data?.message || err.message
      }
    } finally {
      loading.value = false
    }
  }

  async function fetchAllInventory(page = 1) {
    loading.value = true
    error.value = null

    try {
      const response = await api.get('/inventory', {
        params: {page: page - 1, size: pagination.value.pageSize}
      })
      inventoryList.value = response.data.content
      pagination.value.totalPages = response.data.page.totalPages
      pagination.value.totalItems = response.data.page.totalElements
      pagination.value.currentPage = page
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to load inventories.'
    } finally {
      loading.value = false
    }
  }

  function setError(message) {
    error.value = message
  }

  return {stock, loading, error, fetchStock, fetchAllInventory, adjustStock, setError}
})
