import client from './client';

export function searchInterviewQuestions({ category, difficulty, page = 0, size = 10 } = {}) {
  return client
    .get('/interview-questions', {
      params: { category: category || undefined, difficulty: difficulty || undefined, page, size },
    })
    .then((res) => res.data);
}

export function getInterviewQuestionCategories() {
  return client.get('/interview-questions/categories').then((res) => res.data);
}

export function createInterviewQuestion(payload) {
  return client.post('/admin/interview-questions', payload).then((res) => res.data);
}

export function updateInterviewQuestion(questionId, payload) {
  return client.put(`/admin/interview-questions/${questionId}`, payload).then((res) => res.data);
}

export function deleteInterviewQuestion(questionId) {
  return client.delete(`/admin/interview-questions/${questionId}`);
}
