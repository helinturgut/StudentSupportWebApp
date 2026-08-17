import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getDashboard } from '../api/dashboard';
import { getRecommendations, refreshRecommendations } from '../api/recommendations';
import { extractErrorMessage } from '../api/client';
import StatCard from '../components/StatCard';
import Alert from '../components/Alert';

export default function DashboardPage() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const [recommendations, setRecommendations] = useState([]);
  const [recError, setRecError] = useState('');
  const [recLoading, setRecLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  useEffect(() => {
    getDashboard()
      .then(setData)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    getRecommendations()
      .then(setRecommendations)
      .catch((err) => setRecError(extractErrorMessage(err)))
      .finally(() => setRecLoading(false));
  }, []);

  const handleRefresh = () => {
    setRefreshing(true);
    refreshRecommendations()
      .then(setRecommendations)
      .catch((err) => setRecError(extractErrorMessage(err)))
      .finally(() => setRefreshing(false));
  };

  if (loading) return <p>Loading dashboard…</p>;

  return (
    <div className="page-shell page-dashboard">
      <h1>Welcome, {data?.fullName || 'Student'}</h1>
      <Alert>{error}</Alert>
      {data && (
        <>
          <div className="profile-summary card">
            <div>
              <strong>Email:</strong> {data.email}
            </div>
            <div>
              <strong>Course:</strong> {data.course || 'Not set'}
            </div>
            <div>
              <strong>Career goal:</strong> {data.careerGoal || 'Not set'}
            </div>
          </div>
          <div className="stat-grid">
            <StatCard label="Chat sessions" value={data.chatSessionCount} />
            <StatCard label="CV feedback records" value={data.cvFeedbackCount} />
            <StatCard label="Available resources" value={data.availableResourceCount} />
            <StatCard label="Interview questions" value={data.availableInterviewQuestionCount} />
          </div>

          <div className="filter-bar">
            <h2 style={{ margin: 0 }}>Recommended for you</h2>
            <button type="button" className="btn btn-ghost btn-small" onClick={handleRefresh} disabled={refreshing}>
              {refreshing ? 'Refreshing…' : 'Refresh'}
            </button>
          </div>
          <Alert>{recError}</Alert>
          {recLoading ? (
            <p>Loading recommendations…</p>
          ) : recommendations.length ? (
            <div className="card-grid">
              {recommendations.map((r) =>
                r.recommendationType === 'PATHWAY' ? (
                  <Link to={`/career-pathways/${r.pathwayId}`} className="card section-card" key={r.recommendationId}>
                    <span className="badge">Career pathway</span>
                    <h3>{r.pathwayTitle}</h3>
                    <p>{r.reason}</p>
                  </Link>
                ) : (
                  <div className="card" key={r.recommendationId}>
                    <span className="badge">Resource</span>
                    <h3>{r.resourceTitle}</h3>
                    <p>{r.reason}</p>
                  </div>
                )
              )}
            </div>
          ) : (
            <p>
              No recommendations yet — fill in your career goal and skill interests in{' '}
              <Link to="/settings">Settings</Link> to get personalised suggestions.
            </p>
          )}
        </>
      )}
    </div>
  );
}
