import axios from 'axios';

const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();
const shouldUseConfiguredApiBaseUrl =
  configuredApiBaseUrl && (!import.meta.env.PROD || !isLocalApiBaseUrl(configuredApiBaseUrl));

export const API_BASE_URL = normalizeBaseUrl(
  shouldUseConfiguredApiBaseUrl ? configuredApiBaseUrl : import.meta.env.PROD ? '/api' : 'http://localhost:8081/api'
);

const api = axios.create({
  baseURL: API_BASE_URL,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('doubtflow_token');

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

export function getApiErrorMessage(exception, fallbackMessage) {
  if (exception.response?.data?.message) {
    return exception.response.data.message;
  }

  if (!exception.response) {
    return import.meta.env.PROD
      ? 'Could not reach the backend. Please wait a few seconds and try again.'
      : `Could not reach the backend at ${API_BASE_URL}. Start the backend and try again.`;
  }

  return fallbackMessage;
}

function normalizeBaseUrl(url) {
  return url.replace(/\/+$/, '');
}

function isLocalApiBaseUrl(url) {
  return /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?(\/|$)/i.test(url);
}

export default api;
