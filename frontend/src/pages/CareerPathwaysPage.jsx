import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { searchCareerPathwaysByProfession, getCareerPathwayProfessions } from '../api/careerPathways';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';

export default function CareerPathwaysPage() {
  const [professions, setProfessions] = useState([]);
  const [professionsError, setProfessionsError] = useState('');
  const [professionsLoading, setProfessionsLoading] = useState(true);
  const [selectedProfession, setSelectedProfession] = useState(null);

  const [pathways, setPathways] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    setProfessionsLoading(true);
    getCareerPathwayProfessions()
      .then(setProfessions)
      .catch((err) => setProfessionsError(extractErrorMessage(err)))
      .finally(() => setProfessionsLoading(false));
  }, []);

  useEffect(() => {
    if (!selectedProfession) return;
    setLoading(true);
    searchCareerPathwaysByProfession(selectedProfession)
      .then(setPathways)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [selectedProfession]);

  const chooseSection = (profession) => {
    setSelectedProfession(profession);
    setPathways([]);
    setError('');
  };

  const backToSections = () => {
    setSelectedProfession(null);
    setPathways([]);
    setError('');
  };

  if (!selectedProfession) {
    return (
      <div className="page-shell page-pathways">
        <h1>Career Pathways</h1>
        <p>Choose your field of study to see relevant career pathways.</p>
        <Alert>{professionsError}</Alert>
        {professionsLoading ? (
          <p>Loading sections…</p>
        ) : professions.length ? (
          <div className="card-grid">
            {professions.map((profession) => (
              <button
                type="button"
                key={profession}
                className="card section-card"
                onClick={() => chooseSection(profession)}
              >
                <h3>{profession}</h3>
                <span className="btn btn-ghost btn-small">View pathways →</span>
              </button>
            ))}
          </div>
        ) : (
          <p>No career pathway sections are available yet.</p>
        )}
      </div>
    );
  }

  return (
    <div className="page-shell page-pathways">
      <h1>Career Pathways</h1>
      <div className="filter-bar">
        <button type="button" className="btn btn-ghost" onClick={backToSections}>
          ← All sections
        </button>
        <span className="badge">{selectedProfession}</span>
      </div>
      <Alert>{error}</Alert>
      {loading ? (
        <p>Loading career pathways…</p>
      ) : (
        <div className="card-grid">
          {pathways.length ? (
            pathways.map((pathway) => (
              <Link to={`/career-pathways/${pathway.pathwayId}`} className="card section-card" key={pathway.pathwayId}>
                <h3>{pathway.title}</h3>
                <p>{pathway.description}</p>
                <span className="btn btn-ghost btn-small">
                  View interview prep &amp; resources →
                </span>
              </Link>
            ))
          ) : (
            <p>No career pathways found for this section yet.</p>
          )}
        </div>
      )}
    </div>
  );
}
