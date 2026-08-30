import { useEffect, useState } from 'react';
import {
  listInvitedStaff,
  listActiveStaff,
  createStaffAccount,
  resendStaffInvite,
  deleteStaffAccount,
} from '../api/staffAccounts';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';

export default function AdminStaffAccountsPage() {
  const [invited, setInvited] = useState([]);
  const [active, setActive] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [actioningId, setActioningId] = useState(null);

  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [creating, setCreating] = useState(false);

  const load = () => {
    setLoading(true);
    Promise.all([listInvitedStaff(), listActiveStaff()])
      .then(([invitedAccounts, activeAccounts]) => {
        setInvited(invitedAccounts);
        setActive(activeAccounts);
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setCreating(true);
    try {
      const created = await createStaffAccount(fullName, email);
      setInvited((prev) => [created, ...prev]);
      setSuccess(`Invite sent to ${created.email}`);
      setFullName('');
      setEmail('');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setCreating(false);
    }
  };

  const handleResend = async (userId) => {
    setError('');
    setSuccess('');
    setActioningId(userId);
    try {
      const updated = await resendStaffInvite(userId);
      setSuccess(`Invite resent to ${updated.email}`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setActioningId(null);
    }
  };

  const handleDelete = async (userId, fromList) => {
    if (!window.confirm('Permanently delete this staff account? This cannot be undone.')) {
      return;
    }
    setError('');
    setSuccess('');
    setActioningId(userId);
    try {
      await deleteStaffAccount(userId);
      if (fromList === 'invited') {
        setInvited((prev) => prev.filter((account) => account.userId !== userId));
      } else {
        setActive((prev) => prev.filter((account) => account.userId !== userId));
      }
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setActioningId(null);
    }
  };

  if (loading) return <p>Loading staff accounts…</p>;

  return (
    <div className="page-shell page-admin-staff-approvals">
      <h1>Staff Accounts</h1>
      <Alert>{error}</Alert>
      <Alert type="success">{success}</Alert>

      <h2>Create staff account</h2>
      <form className="admin-form card" onSubmit={handleCreate}>
        <label className="field">
          <span>Full name</span>
          <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
        </label>
        <label className="field">
          <span>Email</span>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <button type="submit" className="btn btn-primary" disabled={creating}>
          {creating ? 'Sending invite…' : 'Create staff account'}
        </button>
      </form>

      <h2>Invitation pending</h2>
      {invited.length === 0 ? (
        <p>No pending invitations.</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Invited</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {invited.map((account) => (
              <tr key={account.userId}>
                <td>{account.fullName}</td>
                <td>{account.email}</td>
                <td>{new Date(account.createdAt).toLocaleString('en-GB')}</td>
                <td>
                  <button
                    type="button"
                    className="btn btn-ghost"
                    disabled={actioningId === account.userId}
                    onClick={() => handleResend(account.userId)}
                  >
                    Resend invite
                  </button>
                  <button
                    type="button"
                    className="btn btn-danger"
                    disabled={actioningId === account.userId}
                    onClick={() => handleDelete(account.userId, 'invited')}
                  >
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <h2>Active</h2>
      {active.length === 0 ? (
        <p>No active staff to show.</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Email</th>
              <th>Joined</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {active.map((account) => (
              <tr key={account.userId}>
                <td>{account.fullName}</td>
                <td>{account.email}</td>
                <td>{new Date(account.createdAt).toLocaleString('en-GB')}</td>
                <td>
                  <button
                    type="button"
                    className="btn btn-danger"
                    disabled={actioningId === account.userId}
                    onClick={() => handleDelete(account.userId, 'active')}
                    title="Permanently delete this account from the system"
                  >
                    Delete permanently
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
