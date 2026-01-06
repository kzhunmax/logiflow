import api from "@/services/api.js";

export const authService = {
  login(username, password) {
    return api.post('/auth/login', { username, password });
  }
};

