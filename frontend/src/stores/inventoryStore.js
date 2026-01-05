import {defineStore} from "pinia";
import {ref} from "vue";
import {inventoryService} from "@/services/inventoryService.js";

export const useInventoryStore = defineStore('inventory', () => {
  const stock = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const inventoryMap = ref({})

  async function fetchStock(sku) {
    loading.value = true
    error.value = null

    try {
      const response = await inventoryService.getStock(sku)
      stock.value = response.data
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function fetchInventoryBySKUs(skus) {
    if (!skus || skus.length === 0) {
      inventoryMap.value = {}
      return
    }

    loading.value = true
    error.value = null

    try {
      const response = await inventoryService.getBySKUs(skus)
      const map = {}
      response.data.forEach(item => {
        map[item.sku] = item.availableQuantity
      })
      inventoryMap.value = map
    } catch (err) {
      error.value = err.response?.data?.message || 'Failed to load inventory.'
    } finally {
      loading.value = false
    }
  }

  async function adjustStock(payload) {
    loading.value = true
    error.value = null

    try {
      await inventoryService.adjustStock(payload)
    } catch (err) {
      if (err.response?.status === 409) {
        error.value = 'Cannot remove more stock than available'
      } else {
        error.value = err.response?.data?.message || err.message
      }
      throw err
    } finally {
      loading.value = false
    }
  }

  function setError(message) {
    error.value = message
  }

  function getStockBySku(sku) {
    return inventoryMap.value[sku] ?? 0
  }

  return {
    stock,
    loading,
    error,
    inventoryMap,
    fetchStock,
    fetchInventoryBySKUs,
    adjustStock,
    setError,
    getStockBySku
  }
})
