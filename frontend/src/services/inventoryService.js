import api from "@/services/api.js";

export const inventoryService = {
  getStock(sku) {
    return api.get(`/inventory/${sku}`);
  },

  adjustStock(payload) {
    return api.post('/inventory/stock', payload);
  },

  getAll(page, size) {
    return api.get('/inventory', {
      params: {page, size}
    })
  }
}
