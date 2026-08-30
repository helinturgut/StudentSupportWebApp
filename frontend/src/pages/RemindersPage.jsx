import { useEffect, useState } from 'react';
import {
  listReminders,
  createReminder,
  updateReminder,
  toggleReminderComplete,
  deleteReminder,
} from '../api/reminders';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';

const emptyForm = { title: '', description: '', dueDate: '' };

function formatDueDate(value) {
  return new Date(value).toLocaleDateString('en-GB', { month: 'short', day: 'numeric', year: 'numeric' });
}

function isOverdue(reminder) {
  if (reminder.completed) return false;
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  return new Date(reminder.dueDate) < today;
}

export default function RemindersPage() {
  const [reminders, setReminders] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    listReminders()
      .then(setReminders)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    setError('');
    const action = editingId ? updateReminder(editingId, form) : createReminder(form);
    action
      .then(() => {
        resetForm();
        load();
      })
      .catch((err) => setError(extractErrorMessage(err)));
  };

  const handleEdit = (reminder) => {
    setEditingId(reminder.reminderId);
    setForm({
      title: reminder.title,
      description: reminder.description || '',
      dueDate: reminder.dueDate,
    });
  };

  const handleToggle = (reminderId) => {
    setError('');
    toggleReminderComplete(reminderId)
      .then(load)
      .catch((err) => setError(extractErrorMessage(err)));
  };

  const handleDelete = (reminderId) => {
    if (!window.confirm('Delete this reminder?')) return;
    setError('');
    deleteReminder(reminderId)
      .then(load)
      .catch((err) => setError(extractErrorMessage(err)));
  };

  return (
    <div className="page-shell page-reminders">
      <h1>Reminders</h1>
      <p>Keep track of learning goals and application deadlines.</p>
      <Alert>{error}</Alert>

      <form className="admin-form card" onSubmit={handleSubmit}>
        <h2>{editingId ? 'Edit reminder' : 'Add a reminder'}</h2>
        <label className="field">
          <span>Title</span>
          <input
            type="text"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            required
          />
        </label>
        <label className="field">
          <span>Description</span>
          <textarea
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </label>
        <label className="field">
          <span>Due date</span>
          <input
            type="date"
            value={form.dueDate}
            onChange={(e) => setForm({ ...form, dueDate: e.target.value })}
            required
          />
        </label>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            {editingId ? 'Save changes' : 'Add reminder'}
          </button>
          {editingId && (
            <button type="button" className="btn btn-ghost" onClick={resetForm}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {loading ? (
        <p>Loading reminders…</p>
      ) : reminders.length ? (
        <div className="card-grid">
          {reminders.map((r) => (
            <div className="card" key={r.reminderId}>
              <span className={`badge ${r.completed ? 'difficulty-easy' : isOverdue(r) ? 'difficulty-hard' : ''}`}>
                {r.completed ? 'Done' : isOverdue(r) ? 'Overdue' : formatDueDate(r.dueDate)}
              </span>
              <h3 style={r.completed ? { textDecoration: 'line-through', opacity: 0.6 } : undefined}>
                {r.title}
              </h3>
              {r.description && <p>{r.description}</p>}
              <div className="table-actions">
                <button type="button" className="btn btn-ghost btn-small" onClick={() => handleToggle(r.reminderId)}>
                  {r.completed ? 'Mark not done' : 'Mark done'}
                </button>
                <button type="button" className="btn btn-ghost btn-small" onClick={() => handleEdit(r)}>
                  Edit
                </button>
                <button type="button" className="btn btn-danger btn-small" onClick={() => handleDelete(r.reminderId)}>
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p>No reminders yet — add one above.</p>
      )}
    </div>
  );
}
