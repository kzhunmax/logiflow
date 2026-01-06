import { ref, computed } from "vue";
import { defineStore } from "pinia";
import { authService } from "@/services/authService.js";

const TOKEN_KEY = 'logiflow_token';
const USER_KEY = 'logiflow_user';

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || null);
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'));
  const loading = ref(false);
  const error = ref(null);

  const isAuthenticated = computed(() => !!token.value);
  const userRole = computed(() => user.value?.role || null);
  const username = computed(() => user.value?.username || null);

  function setAuth(authToken, userData) {
    token.value = authToken;
    user.value = userData;
    localStorage.setItem(TOKEN_KEY, authToken);
    localStorage.setItem(USER_KEY, JSON.stringify(userData));
  }

  function clearAuth() {
    token.value = null;
    user.value = null;
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  async function login(usernameInput, password) {
    loading.value = true;
    error.value = null;

    try {
      const response = await authService.login(usernameInput, password);
      const { token: authToken, username: userName, role } = response.data;

      setAuth(authToken, { username: userName, role });
      return true;
    } catch (err) {
      error.value = err.response?.data?.message || 'Login failed. Please check your credentials.';
      return false;
    } finally {
      loading.value = false;
    }
  }

  function logout() {
    clearAuth();
  }

  // Check if token is still valid (basic check)
  function checkAuth() {
    const storedToken = localStorage.getItem(TOKEN_KEY);
    if (!storedToken) {
      clearAuth();
      return false;
    }
    return true;
  }

  return {
    token,
    user,
    loading,
    error,
    isAuthenticated,
    userRole,
    username,
    login,
    logout,
    checkAuth,
    clearAuth
  };
});

