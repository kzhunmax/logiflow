import api from "@/services/api.js";

export const productService = {
  getAll(page, size, search) {
    return api.get('/catalog/products', {
      params: {page, size, search: search || undefined}
    })
  },

  getById(productId) {
    return api.get(`/catalog/products/${productId}`)
  },

  create(productData) {
    return api.post('/catalog/products', productData)
  },

  update(productId, productData) {
    return api.put(`/catalog/products/${productId}`, productData)
  },

  delete(productId) {
    return api.delete(`/catalog/products/${productId}`)
  }
}
