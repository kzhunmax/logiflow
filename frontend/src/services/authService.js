import api from "@/services/api.js";

export const authService = {
  login(username, password) {
    return api.post('/auth/login', { username, password });
  },

  refresh() {
    // Refresh token is sent automatically via HTTP-only cookie
    return api.post('/auth/refresh');
  },

  logout() {
    return api.post('/auth/logout');
  }
};

