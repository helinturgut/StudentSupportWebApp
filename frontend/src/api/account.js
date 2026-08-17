import client from './client';

export function getAccount() {
  return client.get('/account').then((res) => res.data);
}

export function updateFullName(fullName) {
  return client.put('/account/name', { fullName }).then((res) => res.data);
}

export function changePassword(currentPassword, newPassword) {
  return client.put('/account/password', { currentPassword, newPassword }).then((res) => res.data);
}

export function deleteAccount(password) {
  return client.delete('/account', { data: { password } }).then((res) => res.data);
}
