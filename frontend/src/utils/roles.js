const ROLE_LABELS = {
  ADMIN: 'Admin',
  STUDENT: 'Student',
  CAREER_SUPPORT_STAFF: 'Career Support Staff',
};

export function formatRole(role) {
  return ROLE_LABELS[role] || role;
}
