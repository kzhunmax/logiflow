import axios from 'axios';

const TOKEN_KEY = 'logiflow_token';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json'
  },
})

// Request interceptor - attach token to every request
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  error => Promise.reject(error)
);

// Response interceptor - handle errors
api.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message

    if (status === 401) {
      // Clear stored auth data on 401
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem('logiflow_user');

      // Redirect to login if not already there
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    } else if (status >= 400 && status < 500) {
      console.error(`Client Error [${status}]: ${message}`);
    } else if (status >= 500) {
      console.error(`Server Error [${status}]: ${message}`);
    } else {
      console.error(`Network Error: ${message}`);
    }
    return Promise.reject(error)
  }
)

export default api;
