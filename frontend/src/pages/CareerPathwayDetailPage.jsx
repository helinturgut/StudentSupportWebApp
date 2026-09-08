import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getCareerPathway } from '../api/careerPathways';
import { searchInterviewQuestions } from '../api/interviewQuestions';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';
import InterviewQuestionCard from '../components/InterviewQuestionCard';

export default function CareerPathwayDetailPage() {
  const { pathwayId } = useParams();

  const [pathway, setPathway] = useState(null);
  const [pathwayError, setPathwayError] = useState('');
  const [pathwayLoading, setPathwayLoading] = useState(true);

  const [questions, setQuestions] = useState([]);
  const [questionsError, setQuestionsError] = useState('');
  const [questionsLoading, setQuestionsLoading] = useState(false);

  useEffect(() => {
    setPathwayLoading(true);
    getCareerPathway(pathwayId)
      .then(setPathway)
      .catch((err) => setPathwayError(extractErrorMessage(err)))
      .finally(() => setPathwayLoading(false));
  }, [pathwayId]);

  useEffect(() => {
    if (!pathway) return;
    setQuestionsLoading(true);
    searchInterviewQuestions({ category: pathway.title, size: 100 })
      .then((data) => setQuestions(data.content))
      .catch((err) => setQuestionsError(extractErrorMessage(err)))
      .finally(() => setQuestionsLoading(false));
  }, [pathway]);

  if (pathwayLoading) return <p>Loading career pathway…</p>;
  if (pathwayError) return <Alert>{pathwayError}</Alert>;
  if (!pathway) return null;

  return (
    <div className="page-shell page-pathways">
      <Link to="/career-pathways" className="btn btn-ghost">
        ← Career pathways
      </Link>
      <span className="badge">{pathway.profession}</span>
      <h1>{pathway.title}</h1>
      <p>{pathway.description}</p>
      {pathway.requiredSkills && (
        <p>
          <strong>Required skills:</strong> {pathway.requiredSkills}
        </p>
      )}

      <h2>Resources</h2>
      {pathway.resources?.length ? (
        <div className="card-grid">
          {pathway.resources.map((r) => (
            <div className="card" key={r.resourceId}>
              <h3>{r.resourceTitle}</h3>
              {r.linkReason && !/^(https?:)?\/\//i.test(r.linkReason.trim()) && <p>{r.linkReason}</p>}
              {r.resourceId && (
                <a href={`/api/resources/${r.resourceId}/open`} target="_blank" rel="noreferrer" className="btn btn-ghost btn-small">
                  Open resource →
                </a>
              )}
            </div>
          ))}
        </div>
      ) : (
        <p>No resources linked to this career pathway yet.</p>
      )}

      <h2>Interview preparation</h2>
      <Alert>{questionsError}</Alert>
      {questionsLoading ? (
        <p>Loading interview questions…</p>
      ) : questions.length ? (
        <div className="card-grid">
          {questions.map((q) => (
            <InterviewQuestionCard key={q.questionId} q={q} />
          ))}
        </div>
      ) : (
        <p>
          No interview questions for "{pathway.title}" yet. Browse the{' '}
          <Link to="/interview-questions">Interview Preparation</Link> section for other roles.
        </p>
      )}
    </div>
  );
}
