import client from './client';

export function getProfile() {
  return client.get('/students/profile').then((res) => res.data);
}

export function updateProfile(payload) {
  return client.put('/students/profile', payload).then((res) => res.data);
}
