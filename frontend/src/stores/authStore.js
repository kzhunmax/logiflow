import { ref, computed } from "vue";
import { defineStore } from "pinia";
import { authService } from "@/services/authService.js";

const USER_KEY = 'logiflow_user';

export const useAuthStore = defineStore('auth', () => {
  // Only store non-sensitive user info in localStorage
  // Tokens are stored in HTTP-only cookies (not accessible via JS)
  const user = ref(JSON.parse(localStorage.getItem(USER_KEY) || 'null'));
  const loading = ref(false);
  const error = ref(null);

  const isAuthenticated = computed(() => !!user.value);
  const userRole = computed(() => user.value?.role || null);
  const username = computed(() => user.value?.username || null);

  // ==================== Auth State Management ====================

  function setUser(userData) {
    user.value = userData;
    localStorage.setItem(USER_KEY, JSON.stringify(userData));
  }

  function clearAuth() {
    user.value = null;
    localStorage.removeItem(USER_KEY);
  }

  // ==================== Auth Actions ====================

  async function login(usernameInput, password) {
    loading.value = true;
    error.value = null;

    try {
      const response = await authService.login(usernameInput, password);
      const { username: userName, role } = response.data;

      // Tokens are automatically set as HTTP-only cookies by the server
      setUser({ username: userName, role });
      return true;
    } catch (err) {
      error.value = err.response?.data?.message || 'Login failed. Please check your credentials.';
      return false;
    } finally {
      loading.value = false;
    }
  }

  async function refresh() {
    try {
      const response = await authService.refresh();
      const { username: userName, role } = response.data;

      setUser({ username: userName, role });
      return true;
    } catch (err) {
      console.error('Token refresh failed:', err);
      clearAuth();
      return false;
    }
  }

  async function logout() {
    try {
      await authService.logout();
    } catch (err) {
      console.error('Logout request failed:', err);
    } finally {
      clearAuth();
    }
  }

  function checkAuth() {
    // With HTTP-only cookies, we can't check token directly
    // We rely on user info in localStorage as a hint
    // The actual auth check happens on API calls
    return !!user.value;
  }

  return {
    user,
    loading,
    error,
    isAuthenticated,
    userRole,
    username,
    login,
    logout,
    refresh,
    checkAuth,
    clearAuth,
    setUser
  };
});

