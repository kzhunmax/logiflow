import axios from 'axios';

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json'
  },
})

api.interceptors.response.use(
  response => response,
  error => {
    const status = error.response?.status
    const message = error.response?.data?.message || error.message

    if (status >= 400 && status < 500) {
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
