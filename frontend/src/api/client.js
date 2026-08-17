import axios from 'axios';

const client = axios.create({
  baseURL: '/api',
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

function clearSessionAndRedirect() {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
  if (!window.location.pathname.startsWith('/login')) {
    window.location.href = '/login';
  }
}

// Shared in-flight refresh call, so concurrent 401s only trigger one refresh request.
let refreshPromise = null;

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const { config, response } = error;

    if (response?.status !== 401) {
      return Promise.reject(error);
    }

    // A 401 from the login form itself just means "wrong credentials" —
    // let the page show that, don't treat it as a session expiry.
    if (config?.url === '/auth/login') {
      return Promise.reject(error);
    }

    // The refresh token itself is invalid/expired, or we already retried
    // once and still failed — the session is genuinely over.
    if (config?.url === '/auth/refresh' || config?._retry) {
      clearSessionAndRedirect();
      return Promise.reject(error);
    }

    const refreshToken = localStorage.getItem('refreshToken');
    if (!refreshToken) {
      clearSessionAndRedirect();
      return Promise.reject(error);
    }

    config._retry = true;

    if (!refreshPromise) {
      refreshPromise = client
        .post('/auth/refresh', { refreshToken })
        .then((res) => {
          localStorage.setItem('token', res.data.token);
          localStorage.setItem('refreshToken', res.data.refreshToken);
          return res.data.token;
        })
        .finally(() => {
          refreshPromise = null;
        });
    }

    return refreshPromise
      .then((newToken) => {
        config.headers.Authorization = `Bearer ${newToken}`;
        return client(config);
      })
      .catch((refreshErr) => {
        clearSessionAndRedirect();
        return Promise.reject(refreshErr);
      });
  }
);

export default client;

export function extractErrorMessage(error) {
  const data = error?.response?.data;
  if (!data) return 'Something went wrong. Please try again.';
  if (data.fieldErrors) {
    return Object.values(data.fieldErrors).join(', ');
  }
  return data.message || 'Something went wrong. Please try again.';
}
