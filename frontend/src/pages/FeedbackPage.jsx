import { useEffect, useState } from 'react';
import { submitFeedback, getOwnFeedback, deleteFeedback } from '../api/feedback';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';

export default function FeedbackPage() {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [ownFeedback, setOwnFeedback] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    getOwnFeedback()
      .then(setOwnFeedback)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleDelete = async (feedbackId) => {
    setError('');
    setSuccess('');
    try {
      await deleteFeedback(feedbackId);
      setOwnFeedback((prev) => prev.filter((f) => f.feedbackId !== feedbackId));
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await submitFeedback(Number(rating), comment);
      setComment('');
      setSuccess('Thank you — your feedback has been submitted.');
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <div className="page-shell page-feedback">
      <h1>Feedback</h1>
      <p>Tell us how useful and usable you find the platform.</p>
      <Alert>{error}</Alert>
      <Alert type="success">{success}</Alert>
      <form className="admin-form card" onSubmit={handleSubmit}>
        <label className="field">
          <span>Rating (1-5)</span>
          <select value={rating} onChange={(e) => setRating(e.target.value)}>
            {[1, 2, 3, 4, 5].map((n) => (
              <option key={n} value={n}>
                {n}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          <span>Comment</span>
          <textarea value={comment} onChange={(e) => setComment(e.target.value)} />
        </label>
        <button type="submit" className="btn btn-primary">
          Submit feedback
        </button>
      </form>

      <h2>Your previous feedback</h2>
      {loading ? (
        <p>Loading…</p>
      ) : ownFeedback.length ? (
        <table className="admin-table">
          <thead>
            <tr>
              <th>Rating</th>
              <th>Comment</th>
              <th>Submitted</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {ownFeedback.map((f) => (
              <tr key={f.feedbackId}>
                <td>{f.rating}</td>
                <td>{f.comment}</td>
                <td>{new Date(f.createdAt).toLocaleString('en-GB')}</td>
                <td>
                  <button type="button" className="btn btn-danger" onClick={() => handleDelete(f.feedbackId)}>
                    Delete
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>You haven&apos;t submitted any feedback yet.</p>
      )}
    </div>
  );
}
