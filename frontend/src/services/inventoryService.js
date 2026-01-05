import api from "@/services/api.js";

export const inventoryService = {
  getStock(sku) {
    return api.get(`/inventory/${sku}`);
  },

  adjustStock(payload) {
    return api.post('/inventory/stock', payload);
  },

  getBySKUs(skus) {
    if (!skus || skus.length === 0) return Promise.resolve({data: []})
    return api.get('/inventory/batch', {
      params: {skus: skus.join(',')}
    })
  }
}
