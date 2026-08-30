import client from './client';

export function getAnalytics() {
  return client.get('/admin/analytics').then((res) => res.data);
}
