import { useEffect, useState } from 'react';
import { getAnalytics } from '../api/analytics';
import { extractErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import Alert from '../components/Alert';
import StatCard from '../components/StatCard';

export default function AdminAnalyticsPage() {
  const { isStaff } = useAuth();
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getAnalytics()
      .then(setData)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p>Loading analytics…</p>;

  return (
    <div className="page-shell page-analytics">
      <h1>{isStaff ? 'Staff Analytics' : 'Admin Analytics'}</h1>
      <Alert>{error}</Alert>
      {data && (
        <div className="stat-grid">
          <StatCard label="Users" value={data.userCount} />
          <StatCard label="Resources" value={data.resourceCount} />
          <StatCard label="Chat sessions" value={data.chatSessionCount} />
          <StatCard label="Career pathways" value={data.careerPathwayCount} />
          <StatCard label="Interview questions" value={data.interviewQuestionCount} />
          <StatCard label="Feedback submissions" value={data.feedbackCount} />
          <StatCard
            label="Average feedback rating"
            value={data.averageFeedbackRating != null ? data.averageFeedbackRating.toFixed(2) : 'N/A'}
          />
        </div>
      )}
    </div>
  );
}
