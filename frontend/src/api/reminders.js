import client from './client';

export function listReminders() {
  return client.get('/reminders').then((res) => res.data);
}

export function createReminder(payload) {
  return client.post('/reminders', payload).then((res) => res.data);
}

export function updateReminder(reminderId, payload) {
  return client.put(`/reminders/${reminderId}`, payload).then((res) => res.data);
}

export function toggleReminderComplete(reminderId) {
  return client.patch(`/reminders/${reminderId}/complete`).then((res) => res.data);
}

export function deleteReminder(reminderId) {
  return client.delete(`/reminders/${reminderId}`);
}
