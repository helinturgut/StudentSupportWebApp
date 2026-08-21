import client from './client';

export function getChatSessions() {
  return client.get('/chat/sessions').then((res) => res.data);
}

export function createChatSession() {
  return client.post('/chat/sessions').then((res) => res.data);
}

export function deleteChatSession(sessionId) {
  return client.delete(`/chat/sessions/${sessionId}`);
}

export function getChatMessages(sessionId) {
  return client.get(`/chat/sessions/${sessionId}/messages`).then((res) => res.data);
}

export function sendChatMessage(sessionId, messageText) {
  return client.post(`/chat/sessions/${sessionId}/messages`, { messageText }).then((res) => res.data);
}

export function uploadChatFile(sessionId, file) {
  const formData = new FormData();
  formData.append('file', file);
  return client
    .post(`/chat/sessions/${sessionId}/upload`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((res) => res.data);
}

export function downloadChatAttachment(messageId, fileName) {
  return client.get(`/chat/attachments/${messageId}`, { responseType: 'blob' }).then((res) => {
    const url = window.URL.createObjectURL(res.data);
    const link = document.createElement('a');
    link.href = url;
    link.download = fileName || 'attachment';
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  });
}
