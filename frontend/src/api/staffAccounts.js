import client from './client';

export function listInvitedStaff() {
  return client.get('/admin/staff-accounts').then((res) => res.data);
}

export function listActiveStaff() {
  return client.get('/admin/staff-accounts/active').then((res) => res.data);
}

export function createStaffAccount(fullName, email) {
  return client.post('/admin/staff-accounts', { fullName, email }).then((res) => res.data);
}

export function resendStaffInvite(userId) {
  return client.post(`/admin/staff-accounts/${userId}/resend-invite`).then((res) => res.data);
}

export function deleteStaffAccount(userId) {
  return client.delete(`/admin/staff-accounts/${userId}`);
}
