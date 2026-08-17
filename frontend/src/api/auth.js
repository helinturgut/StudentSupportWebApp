import client from './client';

export function login(email, password) {
  return client.post('/auth/login', { email, password }).then((res) => res.data);
}

export function register(fullName, email, password) {
  return client.post('/auth/register', { fullName, email, password }).then((res) => res.data);
}

export function forgotPassword(email) {
  return client.post('/auth/forgot-password', { email }).then((res) => res.data);
}

export function resetPassword(token, newPassword) {
  return client.post('/auth/reset-password', { token, newPassword }).then((res) => res.data);
}

export function refreshAccessToken(refreshToken) {
  return client.post('/auth/refresh', { refreshToken }).then((res) => res.data);
}

export function logout(refreshToken) {
  return client.post('/auth/logout', { refreshToken });
}
