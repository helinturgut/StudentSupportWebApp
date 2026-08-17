import client from './client';

export function getDashboard() {
  return client.get('/students/dashboard').then((res) => res.data);
}
