import client from './client';

export function listCvFeedback() {
  return client.get('/cv-feedback').then((res) => res.data);
}

export function submitCvFeedback(inputText, detailed = false) {
  return client.post('/cv-feedback', { inputText, detailed }).then((res) => res.data);
}

export function uploadCvFile(file, detailed = false) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('detailed', detailed);
  return client
    .post('/cv-feedback/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((res) => res.data);
}

export function deleteCvFeedback(cvFeedbackId) {
  return client.delete(`/cv-feedback/${cvFeedbackId}`).then((res) => res.data);
}
