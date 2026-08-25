import { useEffect, useState } from 'react';
import {
  searchCareerPathways,
  createCareerPathway,
  updateCareerPathway,
  deleteCareerPathway,
  linkResourceToPathway,
  unlinkResourceFromPathway,
} from '../api/careerPathways';
import { searchResources } from '../api/resources';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';

const emptyForm = { profession: '', title: '', description: '', requiredSkills: '' };
const emptyLinkForm = { resourceId: '', linkReason: '' };

export default function AdminCareerPathwaysPage() {
  const [pathways, setPathways] = useState([]);
  const [resources, setResources] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [linkForms, setLinkForms] = useState({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    Promise.all([searchCareerPathways(''), searchResources({ size: 100 })])
      .then(([pathwayData, resourceData]) => {
        setPathways(pathwayData);
        setResources(resourceData.content);
      })
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
        await updateCareerPathway(editingId, form);
      } else {
        await createCareerPathway(form);
      }
      resetForm();
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleEdit = (pathway) => {
    setEditingId(pathway.pathwayId);
    setForm({
      profession: pathway.profession || '',
      title: pathway.title,
      description: pathway.description || '',
      requiredSkills: pathway.requiredSkills || '',
    });
  };

  const handleDelete = async (pathwayId) => {
    if (!window.confirm('Delete this career pathway?')) return;
    setError('');
    try {
      await deleteCareerPathway(pathwayId);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const getLinkForm = (pathwayId) => linkForms[pathwayId] || emptyLinkForm;

  const setLinkForm = (pathwayId, updates) => {
    setLinkForms((prev) => ({ ...prev, [pathwayId]: { ...getLinkForm(pathwayId), ...updates } }));
  };

  const handleLink = async (pathwayId) => {
    const linkForm = getLinkForm(pathwayId);
    if (!linkForm.resourceId) return;
    setError('');
    try {
      await linkResourceToPathway(pathwayId, Number(linkForm.resourceId), linkForm.linkReason);
      setLinkForms((prev) => ({ ...prev, [pathwayId]: emptyLinkForm }));
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleUnlink = async (pathwayId, resourceId) => {
    setError('');
    try {
      await unlinkResourceFromPathway(pathwayId, resourceId);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page-shell page-admin-pathways">
      <h1>Manage Career Pathways</h1>
      <Alert>{error}</Alert>
      <form className="admin-form card" onSubmit={handleSubmit}>
        <h2>{editingId ? 'Edit pathway' : 'Add a career pathway'}</h2>
        <label className="field">
          <span>Profession / field</span>
          <input
            type="text"
            placeholder="e.g. Software Engineering"
            value={form.profession}
            onChange={(e) => setForm({ ...form, profession: e.target.value })}
            required
          />
        </label>
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
          <span>Required skills</span>
          <input
            type="text"
            value={form.requiredSkills}
            onChange={(e) => setForm({ ...form, requiredSkills: e.target.value })}
          />
        </label>
        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            {editingId ? 'Save changes' : 'Create pathway'}
          </button>
          {editingId && (
            <button type="button" className="btn btn-ghost" onClick={resetForm}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {loading ? (
        <p>Loading career pathways…</p>
      ) : (
        <div className="card-grid">
          {pathways.map((pathway) => {
            const linkForm = getLinkForm(pathway.pathwayId);
            return (
              <div className="card" key={pathway.pathwayId}>
                <span className="badge">{pathway.profession}</span>
                <h3>{pathway.title}</h3>
                <p>{pathway.description}</p>
                <div className="table-actions">
                  <button type="button" className="btn btn-ghost" onClick={() => handleEdit(pathway)}>
                    Edit
                  </button>
                  <button type="button" className="btn btn-danger" onClick={() => handleDelete(pathway.pathwayId)}>
                    Delete
                  </button>
                </div>

                <strong>Linked resources</strong>
                <ul>
                  {pathway.resources?.map((r) => (
                    <li key={r.resourceId}>
                      {r.resourceTitle}
                      <button
                        type="button"
                        className="btn btn-ghost btn-small"
                        onClick={() => handleUnlink(pathway.pathwayId, r.resourceId)}
                      >
                        Unlink
                      </button>
                    </li>
                  ))}
                  {!pathway.resources?.length && <li>No resources linked yet.</li>}
                </ul>

                <div className="link-form">
                  <select
                    value={linkForm.resourceId}
                    onChange={(e) => setLinkForm(pathway.pathwayId, { resourceId: e.target.value })}
                  >
                    <option value="">Select a resource to link…</option>
                    {resources.map((r) => (
                      <option key={r.resourceId} value={r.resourceId}>
                        {r.title}
                      </option>
                    ))}
                  </select>
                  <input
                    type="text"
                    placeholder="Reason for linking (optional)"
                    value={linkForm.linkReason}
                    onChange={(e) => setLinkForm(pathway.pathwayId, { linkReason: e.target.value })}
                  />
                  <button type="button" className="btn btn-primary btn-small" onClick={() => handleLink(pathway.pathwayId)}>
                    Link
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
