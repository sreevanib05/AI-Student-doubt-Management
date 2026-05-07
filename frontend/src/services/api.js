import axios from 'axios';

const DEFAULT_RENDER_API_BASE_URL = 'https://doubtflow-ai-backend.onrender.com/api';
const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();

const API_BASE_URL_CANDIDATES = buildApiBaseUrlCandidates();
export const API_BASE_URL = API_BASE_URL_CANDIDATES[0];

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

api.interceptors.response.use(
  (response) => response,
  (exception) => {
    const fallbackBaseUrl = getNextFallbackBaseUrl(exception);

    if (fallbackBaseUrl) {
      exception.config.baseURL = fallbackBaseUrl;
      exception.config._triedBaseUrls = [...getTriedBaseUrls(exception.config), fallbackBaseUrl];
      return api.request(exception.config);
    }

    return Promise.reject(exception);
  }
);

export function getApiErrorMessage(exception, fallbackMessage) {
  if (exception.response?.data?.message) {
    return exception.response.data.message;
  }

  if (!exception.response) {
    const triedBaseUrls = getTriedBaseUrls(exception.config);

    return import.meta.env.PROD
      ? `Could not reach the backend. Tried ${formatBaseUrls(triedBaseUrls)}. Please wait a few seconds and try again.`
      : `Could not reach the backend at ${formatBaseUrls(triedBaseUrls)}. Start the backend and try again.`;
  }

  return fallbackMessage;
}

function buildApiBaseUrlCandidates() {
  const candidates = [];

  if (import.meta.env.PROD) {
    candidates.push(DEFAULT_RENDER_API_BASE_URL);
    candidates.push('/api');
  } else {
    candidates.push('http://localhost:8081/api');
    candidates.push(DEFAULT_RENDER_API_BASE_URL);
  }

  if (isUsableConfiguredApiBaseUrl(configuredApiBaseUrl)) {
    candidates.push(configuredApiBaseUrl);
  }

  return [...new Set(candidates.map(normalizeBaseUrl))];
}

function getNextFallbackBaseUrl(exception) {
  if (exception.response || !exception.config || isAbsoluteUrl(exception.config.url)) {
    return null;
  }

  const triedBaseUrls = getTriedBaseUrls(exception.config);
  return API_BASE_URL_CANDIDATES.find((baseUrl) => !triedBaseUrls.includes(baseUrl)) || null;
}

function getTriedBaseUrls(config) {
  if (config?._triedBaseUrls?.length) {
    return config._triedBaseUrls;
  }

  return [normalizeBaseUrl(config?.baseURL || API_BASE_URL)];
}

function formatBaseUrls(baseUrls) {
  return baseUrls.join(' and ');
}

function isUsableConfiguredApiBaseUrl(url) {
  return Boolean(url) && !isPlaceholderApiBaseUrl(url) && (!import.meta.env.PROD || !isLocalApiBaseUrl(url));
}

function normalizeBaseUrl(url) {
  return url.replace(/\/+$/, '');
}

function isAbsoluteUrl(url = '') {
  return /^[a-z][a-z\d+\-.]*:\/\//i.test(url);
}

function isLocalApiBaseUrl(url) {
  return /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?(\/|$)/i.test(url);
}

function isPlaceholderApiBaseUrl(url) {
  return /your-render-backend|example\.com/i.test(url);
}

export default api;
