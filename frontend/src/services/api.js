import axios from 'axios';

const USER_KEY = 'logiflow_user';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // Important: send cookies with requests
})

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, success = false) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

// Response interceptor - handle errors and auto-refresh
api.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;
    const status = error.response?.status;
    const message = error.response?.data?.message || error.message;

    // Handle 401 - try to refresh token
    if (status === 401 && !originalRequest._retry) {
      // Don't retry refresh or login endpoints
      if (originalRequest.url === '/auth/refresh' || originalRequest.url === '/auth/login') {
        clearUserData();
        redirectToLogin();
        return Promise.reject(error);
      }

      if (isRefreshing) {
        // Queue requests while refreshing
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then(() => {
          return api(originalRequest);
        }).catch(err => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        // Refresh token is sent automatically via cookie
        const response = await api.post('/auth/refresh');
        const { username, role } = response.data;

        // Update user info
        localStorage.setItem(USER_KEY, JSON.stringify({ username, role }));

        processQueue(null, true);
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, false);
        clearUserData();
        redirectToLogin();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // Handle other errors
    if (status >= 400 && status < 500) {
      console.error(`Client Error [${status}]: ${message}`);
    } else if (status >= 500) {
      console.error(`Server Error [${status}]: ${message}`);
    } else {
      console.error(`Network Error: ${message}`);
    }

    return Promise.reject(error);
  }
);

function clearUserData() {
  localStorage.removeItem(USER_KEY);
}

function redirectToLogin() {
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

export default api;
