// src/plugins/axios.js
import axios from 'axios';


const _axios = axios.create({
  // baseURL: process.env.baseURL || process.env.apiUrl || "",
  // timeout: 60 * 1000, // Timeout
  // withCredentials: true, // Check cross-site Access-Control
});

// Add a request interceptor
_axios.interceptors.request.use(
  function(config) {
    return config;
  },
  function(error) {
    return Promise.reject(error);
  }
);

_axios.interceptors.response.use(
  function(response) {
    return response;
  },
  function(error) {
    return Promise.reject(error);
  }
);

export default _axios;
