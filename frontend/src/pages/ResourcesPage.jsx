import { useEffect, useState } from 'react';
import { searchResources } from '../api/resources';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';

export default function ResourcesPage() {
  const [keyword, setKeyword] = useState('');
  const [category, setCategory] = useState('');
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    searchResources({ keyword, category, page, size: 9 })
      .then(setResult)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [keyword, category, page]);

  const handleFilterSubmit = (e) => {
    e.preventDefault();
    setPage(0);
  };

  return (
    <div className="page-shell page-resources">
      <h1>Learning &amp; Career Resources</h1>
      <form className="filter-bar" onSubmit={handleFilterSubmit}>
        <input
          type="text"
          placeholder="Search by keyword"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <input
          type="text"
          placeholder="Filter by category"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
        />
        <button type="submit" className="btn btn-primary">
          Search
        </button>
      </form>
      <Alert>{error}</Alert>
      {loading ? (
        <p>Loading resources…</p>
      ) : (
        <>
          <div className="card-grid">
            {result?.content?.length ? (
              result.content.map((resource) => (
                <div className="card" key={resource.resourceId}>
                  <h3>{resource.title}</h3>
                  {resource.category && <span className="badge">{resource.category}</span>}
                  <p>{resource.description}</p>
                  {resource.url && (
                    <a href={resource.url} target="_blank" rel="noreferrer">
                      Open resource
                    </a>
                  )}
                  <div className="card-meta">Added by {resource.createdByName}</div>
                </div>
              ))
            ) : (
              <p>No resources found.</p>
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
