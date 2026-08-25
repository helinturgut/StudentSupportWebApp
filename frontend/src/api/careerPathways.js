import client from './client';

export function searchCareerPathways(term) {
  return client.get('/career-pathways', { params: { term: term || undefined } }).then((res) => res.data);
}

export function searchCareerPathwaysByProfession(profession) {
  return client.get('/career-pathways', { params: { profession } }).then((res) => res.data);
}

export function getCareerPathwayProfessions() {
  return client.get('/career-pathways/professions').then((res) => res.data);
}

export function getCareerPathway(pathwayId) {
  return client.get(`/career-pathways/${pathwayId}`).then((res) => res.data);
}

export function createCareerPathway(payload) {
  return client.post('/admin/career-pathways', payload).then((res) => res.data);
}

export function updateCareerPathway(pathwayId, payload) {
  return client.put(`/admin/career-pathways/${pathwayId}`, payload).then((res) => res.data);
}

export function deleteCareerPathway(pathwayId) {
  return client.delete(`/admin/career-pathways/${pathwayId}`);
}

export function linkResourceToPathway(pathwayId, resourceId, linkReason) {
  return client
    .post(`/admin/career-pathways/${pathwayId}/resources`, { resourceId, linkReason })
    .then((res) => res.data);
}

export function unlinkResourceFromPathway(pathwayId, resourceId) {
  return client.delete(`/admin/career-pathways/${pathwayId}/resources/${resourceId}`);
}
