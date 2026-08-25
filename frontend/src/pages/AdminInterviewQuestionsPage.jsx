import { Fragment, useEffect, useMemo, useState } from 'react';
import {
  searchInterviewQuestions,
  createInterviewQuestion,
  updateInterviewQuestion,
  deleteInterviewQuestion,
} from '../api/interviewQuestions';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';

const emptyForm = { category: '', questionText: '', guidanceText: '', difficulty: 'EASY', options: [] };
const emptyOption = { optionText: '', correct: false };

export default function AdminInterviewQuestionsPage() {
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [collapsed, setCollapsed] = useState({});

  const load = () => {
    setLoading(true);
    searchInterviewQuestions({ size: 100 })
      .then((data) => setItems(data.content))
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const groupedByCategory = useMemo(() => {
    const term = search.trim().toLowerCase();
    const filtered = term
      ? items.filter(
          (q) => q.category.toLowerCase().includes(term) || q.questionText.toLowerCase().includes(term)
        )
      : items;
    const map = {};
    filtered.forEach((q) => {
      (map[q.category] ||= []).push(q);
    });
    return Object.entries(map).sort(([a], [b]) => a.localeCompare(b));
  }, [items, search]);

  const toggleCategory = (category) => {
    setCollapsed((prev) => ({ ...prev, [category]: !prev[category] }));
  };

  const resetForm = () => {
    setForm(emptyForm);
    setEditingId(null);
  };

  const addOption = () => {
    setForm({ ...form, options: [...form.options, { ...emptyOption }] });
  };

  const updateOption = (index, updates) => {
    setForm({
      ...form,
      options: form.options.map((opt, i) => (i === index ? { ...opt, ...updates } : opt)),
    });
  };

  const setCorrectOption = (index) => {
    setForm({
      ...form,
      options: form.options.map((opt, i) => ({ ...opt, correct: i === index })),
    });
  };

  const removeOption = (index) => {
    setForm({ ...form, options: form.options.filter((_, i) => i !== index) });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    const options = form.options
      .filter((opt) => opt.optionText.trim())
      .map((opt) => ({ optionText: opt.optionText.trim(), correct: opt.correct }));
    if (options.length && !options.some((opt) => opt.correct)) {
      setError('Mark one option as the correct answer.');
      return;
    }
    const payload = { ...form, options };
    try {
      if (editingId) {
        await updateInterviewQuestion(editingId, payload);
      } else {
        await createInterviewQuestion(payload);
      }
      resetForm();
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleEdit = (question) => {
    setEditingId(question.questionId);
    setForm({
      category: question.category,
      questionText: question.questionText,
      guidanceText: question.guidanceText || '',
      difficulty: question.difficulty,
      options: (question.options || []).map((opt) => ({ optionText: opt.optionText, correct: opt.correct })),
    });
    setCollapsed((prev) => ({ ...prev, [question.category]: false }));
  };

  const handleDelete = async (questionId) => {
    if (!window.confirm('Delete this question?')) return;
    setError('');
    try {
      await deleteInterviewQuestion(questionId);
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page-shell page-admin-interview">
      <h1>Manage Interview Questions</h1>
      <Alert>{error}</Alert>
      <form className="admin-form card" onSubmit={handleSubmit}>
        <h2>{editingId ? 'Edit question' : 'Add a question'}</h2>
        <label className="field">
          <span>Category</span>
          <input
            type="text"
            value={form.category}
            onChange={(e) => setForm({ ...form, category: e.target.value })}
            required
          />
        </label>
        <label className="field">
          <span>Question</span>
          <textarea
            value={form.questionText}
            onChange={(e) => setForm({ ...form, questionText: e.target.value })}
            required
          />
        </label>
        <label className="field">
          <span>Guidance</span>
          <textarea
            value={form.guidanceText}
            onChange={(e) => setForm({ ...form, guidanceText: e.target.value })}
          />
        </label>
        <label className="field">
          <span>Difficulty</span>
          <select value={form.difficulty} onChange={(e) => setForm({ ...form, difficulty: e.target.value })}>
            <option value="EASY">EASY</option>
            <option value="MEDIUM">MEDIUM</option>
            <option value="HARD">HARD</option>
          </select>
        </label>

        <div className="field">
          <span>Answer options (optional — leave empty for an open-ended question)</span>
          {form.options.map((opt, index) => (
            <div className="option-row" key={index}>
              <input
                type="radio"
                name="correct-option"
                checked={opt.correct}
                onChange={() => setCorrectOption(index)}
                title="Mark as correct answer"
              />
              <input
                type="text"
                placeholder={`Option ${index + 1}`}
                value={opt.optionText}
                onChange={(e) => updateOption(index, { optionText: e.target.value })}
              />
              <button type="button" className="btn btn-ghost btn-small" onClick={() => removeOption(index)}>
                Remove
              </button>
            </div>
          ))}
          <button type="button" className="btn btn-ghost btn-small" onClick={addOption}>
            + Add option
          </button>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn btn-primary">
            {editingId ? 'Save changes' : 'Create question'}
          </button>
          {editingId && (
            <button type="button" className="btn btn-ghost" onClick={resetForm}>
              Cancel
            </button>
          )}
        </div>
      </form>

      {loading ? (
        <p>Loading questions…</p>
      ) : (
        <>
          <div className="filter-bar">
            <input
              type="text"
              placeholder="Search by category or question text…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          {groupedByCategory.length === 0 ? (
            <p>No questions found.</p>
          ) : (
            groupedByCategory.map(([category, questions]) => {
              const isExpanded = search.trim() ? true : !collapsed[category];
              return (
                <div className="admin-group card" key={category}>
                  <button
                    type="button"
                    className="admin-group-header"
                    onClick={() => toggleCategory(category)}
                  >
                    <span>
                      {isExpanded ? '▾' : '▸'} {category}
                    </span>
                    <span className="badge">{questions.length}</span>
                  </button>

                  {isExpanded && (
                    <table className="admin-table">
                      <thead>
                        <tr>
                          <th>Question</th>
                          <th>Type</th>
                          <th>Difficulty</th>
                          <th>Actions</th>
                        </tr>
                      </thead>
                      <tbody>
                        {questions.map((q) => (
                          <Fragment key={q.questionId}>
                            <tr>
                              <td>{q.questionText}</td>
                              <td>{q.options?.length ? `Multiple choice (${q.options.length})` : 'Open-ended'}</td>
                              <td>{q.difficulty}</td>
                              <td className="table-actions">
                                <button type="button" className="btn btn-ghost" onClick={() => handleEdit(q)}>
                                  Edit
                                </button>
                                <button
                                  type="button"
                                  className="btn btn-danger"
                                  onClick={() => handleDelete(q.questionId)}
                                >
                                  Delete
                                </button>
                              </td>
                            </tr>
                            {q.guidanceText && (
                              <tr className="admin-table-guidance">
                                <td></td>
                                <td colSpan={3}>
                                  <strong>Guidance:</strong> {q.guidanceText}
                                </td>
                              </tr>
                            )}
                          </Fragment>
                        ))}
                      </tbody>
                    </table>
                  )}
                </div>
              );
            })
          )}
        </>
      )}
    </div>
  );
}
