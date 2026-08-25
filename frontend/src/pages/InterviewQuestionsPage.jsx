import { useEffect, useState } from 'react';
import { searchInterviewQuestions, getInterviewQuestionCategories } from '../api/interviewQuestions';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';
import InterviewQuestionCard from '../components/InterviewQuestionCard';

const DIFFICULTIES = ['', 'EASY', 'MEDIUM', 'HARD'];

export default function InterviewQuestionsPage() {
  const [categories, setCategories] = useState([]);
  const [categoriesError, setCategoriesError] = useState('');
  const [categoriesLoading, setCategoriesLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState(null);

  const [difficulty, setDifficulty] = useState('');
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setCategoriesLoading(true);
    getInterviewQuestionCategories()
      .then(setCategories)
      .catch((err) => setCategoriesError(extractErrorMessage(err)))
      .finally(() => setCategoriesLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedCategory) return;
    setLoading(true);
    searchInterviewQuestions({ category: selectedCategory, difficulty, page, size: 9 })
      .then(setResult)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [selectedCategory, difficulty, page]);

  const chooseSection = (category) => {
    setSelectedCategory(category);
    setDifficulty('');
    setPage(0);
    setResult(null);
    setError('');
  };

  const backToSections = () => {
    setSelectedCategory(null);
    setResult(null);
    setError('');
  };

  if (!selectedCategory) {
    return (
      <div className="page-shell page-interview">
        <h1>Interview Preparation</h1>
        <p>Choose the area you're preparing for today.</p>
        <Alert>{categoriesError}</Alert>
        {categoriesLoading ? (
          <p>Loading sections…</p>
        ) : categories.length ? (
          <div className="card-grid">
            {categories.map((category) => (
              <button
                type="button"
                key={category}
                className="card section-card"
                onClick={() => chooseSection(category)}
              >
                <h3>{category}</h3>
                <span className="btn btn-ghost btn-small">Start practicing →</span>
              </button>
            ))}
          </div>
        ) : (
          <p>No interview question sections are available yet.</p>
        )}
      </div>
    );
  }

  return (
    <div className="page-shell page-interview">
      <h1>Interview Preparation</h1>
      <div className="filter-bar">
        <button type="button" className="btn btn-ghost" onClick={backToSections}>
          ← All sections
        </button>
        <span className="badge">{selectedCategory}</span>
        <select value={difficulty} onChange={(e) => { setDifficulty(e.target.value); setPage(0); }}>
          {DIFFICULTIES.map((d) => (
            <option key={d} value={d}>
              {d || 'Any difficulty'}
            </option>
          ))}
        </select>
      </div>
      <Alert>{error}</Alert>
      {loading ? (
        <p>Loading questions…</p>
      ) : (
        <>
          <div className="card-grid">
            {result?.content?.length ? (
              result.content.map((q) => <InterviewQuestionCard key={q.questionId} q={q} />)
            ) : (
              <p>No interview questions found for this section yet.</p>
            )}
          </div>
          {result && result.totalPages > 1 && (
            <div className="pagination">
              <button
                type="button"
                className="btn btn-ghost"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                Previous
              </button>
              <span>
                Page {page + 1} of {result.totalPages}
              </span>
              <button
                type="button"
                className="btn btn-ghost"
                disabled={page + 1 >= result.totalPages}
                onClick={() => setPage((p) => p + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
