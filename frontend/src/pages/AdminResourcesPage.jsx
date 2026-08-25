import { useEffect, useState } from 'react';
import { searchResources, createResource, updateResource, deleteResource } from '../api/resources';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';

const emptyForm = { title: '', description: '', category: '', url: '' };

export default function AdminResourcesPage() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    searchResources({ size: 100 })
      .then((data) => setItems(data.content))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editingId) {
        await updateResource(editingId, form);
      } else {
        await createResource(form);
      }
      resetForm();
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleEdit = (resource) => {
    setEditingId(resource.resourceId);
    setForm({
      title: resource.title,
      description: resource.description || '',
      category: resource.category || '',
      url: resource.url || '',
    });
  };

  const handleDelete = async (resourceId) => {
    if (!window.confirm('Delete this resource?')) return;
    setError('');
    try {
      await deleteResource(resourceId);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page-shell page-admin-resources">
      <h1>Manage Resources</h1>
      <Alert>{error}</Alert>
      <form className="admin-form card" onSubmit={handleSubmit}>
        <h2>{editingId ? 'Edit resource' : 'Add a resource'}</h2>
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
          <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </label>
        <label className="field">
          <span>Category</span>
          <input type="text" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} />
        </label>
        <label className="field">
          <span>URL</span>
          <input type="url" value={form.url} onChange={(e) => setForm({ ...form, url: e.target.value })} />
        </label>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            {editingId ? 'Save changes' : 'Create resource'}
          </button>
          {editingId && (
            <button type="button" className="btn btn-ghost" onClick={resetForm}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {loading ? (
        <p>Loading resources…</p>
      ) : (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Title</th>
              <th>Category</th>
              <th>Created by</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map((resource) => (
              <tr key={resource.resourceId}>
                <td>{resource.title}</td>
                <td>{resource.category}</td>
                <td>{resource.createdByName}</td>
                <td className="table-actions">
                  <button type="button" className="btn btn-ghost" onClick={() => handleEdit(resource)}>
                    Edit
                  </button>
                  <button type="button" className="btn btn-danger" onClick={() => handleDelete(resource.resourceId)}>
                    Delete
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
