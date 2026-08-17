import client from './client';

export function getRecommendations() {
  return client.get('/recommendations').then((res) => res.data);
}

export function refreshRecommendations() {
  return client.post('/recommendations/refresh').then((res) => res.data);
}
