import client from './client';

export function searchResources({ keyword, category, page = 0, size = 10 } = {}) {
  return client
    .get('/resources', { params: { keyword: keyword || undefined, category: category || undefined, page, size } })
    .then((res) => res.data);
}

export function getResource(resourceId) {
  return client.get(`/resources/${resourceId}`).then((res) => res.data);
}

export function createResource(payload) {
  return client.post('/admin/resources', payload).then((res) => res.data);
}

export function updateResource(resourceId, payload) {
  return client.put(`/admin/resources/${resourceId}`, payload).then((res) => res.data);
}

export function deleteResource(resourceId) {
  return client.delete(`/admin/resources/${resourceId}`);
}
