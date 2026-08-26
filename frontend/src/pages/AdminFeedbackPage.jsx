import { useEffect, useState } from 'react';
import { getAllFeedback } from '../api/feedback';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';
import StatCard from '../components/StatCard';

export default function AdminFeedbackPage() {
  const [summary, setSummary] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAllFeedback()
      .then(setSummary)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading feedback…</p>;

  return (
    <div className="page-shell page-admin-feedback">
      <h1>Review Feedback</h1>
      <Alert>{error}</Alert>
      {summary && (
        <>
          <div className="stat-grid">
            <StatCard label="Total feedback" value={summary.totalFeedbackCount} />
            <StatCard
              label="Average rating"
              value={summary.averageRating != null ? summary.averageRating.toFixed(2) : 'N/A'}
            />
          </div>
          <table className="admin-table">
            <thead>
              <tr>
                <th>User</th>
                <th>Rating</th>
                <th>Comment</th>
                <th>Submitted</th>
              </tr>
            </thead>
            <tbody>
              {summary.feedback.map((f) => (
                <tr key={f.feedbackId}>
                  <td>{f.userFullName}</td>
                  <td>{f.rating}</td>
                  <td>{f.comment}</td>
                  <td>{new Date(f.createdAt).toLocaleString('en-GB')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}
    </div>
  );
}
