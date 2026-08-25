import { useEffect, useRef, useState } from 'react';
import { deleteCvFeedback, listCvFeedback, submitCvFeedback, uploadCvFile } from '../api/cvFeedback';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';
import CvFeedbackEntry from '../components/CvFeedbackEntry';

export default function CvGuidancePage() {
  const [inputText, setInputText] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [detailed, setDetailed] = useState(false);
  const [history, setHistory] = useState([]);
  const [error, setError] = useState('');
  const [loadingHistory, setLoadingHistory] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const fileInputRef = useRef(null);

  const loadHistory = () => {
    setLoadingHistory(true);
    listCvFeedback()
      .then(setHistory)
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoadingHistory(false));
  };

  useEffect(loadHistory, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (submitting || (!selectedFile && !inputText.trim())) return;
    setError('');
    setSubmitting(true);
    const request = selectedFile
      ? uploadCvFile(selectedFile, detailed)
      : submitCvFeedback(inputText.trim(), detailed);
    request
      .then((feedback) => {
        setHistory((prev) => [feedback, ...prev]);
        setInputText('');
        setSelectedFile(null);
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setSubmitting(false));
  };

  const handleFileChange = (e) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;
    setError('');
    setInputText('');
    setSelectedFile(file);
  };

  const clearSelectedFile = () => setSelectedFile(null);

  const handleDelete = (cvFeedbackId) => {
    if (!window.confirm('Delete this CV feedback record?')) return;
    setError('');
    deleteCvFeedback(cvFeedbackId)
      .then(() => setHistory((prev) => prev.filter((item) => item.cvFeedbackId !== cvFeedbackId)))
      .catch((err) => setError(extractErrorMessage(err)));
  };

  return (
    <div className="page-shell page-cv-guidance">
      <h1>CV Guidance</h1>
      <p>
        Paste your CV, or a section of it (e.g. your work experience or personal statement), and get
        AI-assisted feedback on wording, structure, and impact. You can also upload a CV file directly.
      </p>
      <Alert type="info">
        This feedback is AI-generated and may be inaccurate or incomplete — use it as a starting point,
        not a final judgement, and always double-check important claims yourself.
      </Alert>
      <Alert>{error}</Alert>

      <form className="admin-form card" onSubmit={handleSubmit}>
        <label className="field">
          <span>CV text</span>
          <textarea
            value={inputText}
            onChange={(e) => {
              setInputText(e.target.value);
              if (selectedFile) setSelectedFile(null);
            }}
            placeholder="Paste your CV or CV section here…"
            style={{ minHeight: 180 }}
            disabled={!!selectedFile}
          />
        </label>

        <input
          type="file"
          ref={fileInputRef}
          accept=".pdf,.docx,.txt"
          style={{ display: 'none' }}
          onChange={handleFileChange}
        />
        <div className="form-actions">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => fileInputRef.current?.click()}
            disabled={submitting}
          >
            📎 Upload CV file (PDF, DOCX, or TXT)
          </button>
        </div>
        {selectedFile && (
          <p>
            Selected file: <strong>{selectedFile.name}</strong>{' '}
            <button type="button" className="btn btn-ghost btn-small" onClick={clearSelectedFile}>
              Remove
            </button>
          </p>
        )}

        <label className="field-checkbox">
          <input type="checkbox" checked={detailed} onChange={(e) => setDetailed(e.target.checked)} />
          <span>
            Give me detailed feedback — quote exact lines and show what to change them to, not just
            general advice
          </span>
        </label>

        <div className="form-actions">
          <button
            type="submit"
            className="btn btn-primary"
            disabled={submitting || (!selectedFile && !inputText.trim())}
          >
            {submitting ? 'Analysing…' : 'Get feedback'}
          </button>
        </div>
      </form>

      <h2>Previous feedback</h2>
      {loadingHistory ? (
        <p>Loading history…</p>
      ) : history.length ? (
        <div className="cv-feedback-list">
          {history.map((item) => (
            <CvFeedbackEntry key={item.cvFeedbackId} item={item} onDelete={handleDelete} />
          ))}
        </div>
      ) : (
        <p>No CV feedback yet — submit your CV above to get started.</p>
      )}
    </div>
  );
}
