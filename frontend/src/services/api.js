import axios from 'axios';

const DEFAULT_RENDER_API_BASE_URL = 'https://doubtflow-ai-backend.onrender.com/api';
const LOCAL_API_BASE_URL = 'http://localhost:8081/api';
const RELATIVE_API_BASE_URL = '/api';
const REQUEST_TIMEOUT_MS = 20000;
export const LOGIN_TIMEOUT_MS = 60000;
const WARM_UP_TIMEOUT_MS = 6000;
const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL?.trim();

const API_BASE_URL_CANDIDATES = buildApiBaseUrlCandidates();
export const API_BASE_URL = API_BASE_URL_CANDIDATES[0];

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT_MS,
  transitional: {
    clarifyTimeoutError: true,
  },
});

api.interceptors.request.use((config) => {
  config._triedBaseUrls = getTriedBaseUrls(config);

  if (config._skipAuth) {
    return config;
  }

  const token = localStorage.getItem('doubtflow_token');

  if (token) {
    config.headers = config.headers || {};
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
    const reason = isTimeoutError(exception)
      ? 'The backend did not respond quickly enough.'
      : 'Could not reach the backend.';

    return import.meta.env.PROD
      ? `${reason} Tried ${formatBaseUrls(triedBaseUrls)}. Please wait a few seconds and try again.`
      : `${reason} Tried ${formatBaseUrls(triedBaseUrls)}. Start the backend and try again.`;
  }

  if (exception.response.status === 404) {
    return `Backend route not found. Check that VITE_API_BASE_URL ends with /api and points to your Render backend.`;
  }

  if (exception.response.status >= 500) {
    return `Backend error (${exception.response.status}). Check Render logs and environment variables.`;
  }

  return fallbackMessage;
}

let warmUpPromise = null;

export function warmUpApi() {
  if (!warmUpPromise) {
    warmUpPromise = api.get('/health', {
      _skipAuth: true,
      timeout: WARM_UP_TIMEOUT_MS,
    }).catch(() => null)
      .finally(() => {
        warmUpPromise = null;
      });
  }

  return warmUpPromise;
}

function buildApiBaseUrlCandidates() {
  const candidates = [];

  if (isUsableConfiguredApiBaseUrl(configuredApiBaseUrl)) {
    candidates.push(configuredApiBaseUrl);
  }

  if (import.meta.env.PROD) {
    candidates.push(RELATIVE_API_BASE_URL);
    candidates.push(DEFAULT_RENDER_API_BASE_URL);
  } else {
    candidates.push(LOCAL_API_BASE_URL);
    candidates.push(DEFAULT_RENDER_API_BASE_URL);
  }

  return [...new Set(candidates.map(normalizeBaseUrl))];
}

function getNextFallbackBaseUrl(exception) {
  if (
    exception.response ||
    !exception.config ||
    isAbsoluteUrl(exception.config.url) ||
    !canRetryWithFallback(exception.config)
  ) {
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

function canRetryWithFallback(config) {
  const method = (config?.method || 'get').toLowerCase();

  if (['get', 'head', 'options'].includes(method)) {
    return true;
  }

  return isLoginEndpoint(config?.url);
}

function isLoginEndpoint(url = '') {
  return ['/auth/login', '/students/login'].includes(url);
}

function isTimeoutError(exception) {
  return exception.code === 'ECONNABORTED' || exception.code === 'ETIMEDOUT';
}

function isLocalApiBaseUrl(url) {
  return /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?(\/|$)/i.test(url);
}

function isPlaceholderApiBaseUrl(url) {
  return /your-render-backend|example\.com/i.test(url);
}

export default api;
