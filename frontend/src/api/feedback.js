import client from './client';

export function submitFeedback(rating, comment) {
  return client.post('/feedback', { rating, comment }).then((res) => res.data);
}

export function getOwnFeedback() {
  return client.get('/feedback').then((res) => res.data);
}

export function deleteFeedback(feedbackId) {
  return client.delete(`/feedback/${feedbackId}`).then((res) => res.data);
}

export function getAllFeedback() {
  return client.get('/admin/feedback').then((res) => res.data);
}
