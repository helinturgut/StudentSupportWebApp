import { useState } from 'react';
import AiMarkdown from './AiMarkdown';

const SUBMISSION_PREVIEW_LENGTH = 140;

function formatDate(value) {
  if (!value) return '';
  const date = new Date(value);
  return date.toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' });
}

export default function CvFeedbackEntry({ item, onDelete }) {
  const [showSubmission, setShowSubmission] = useState(false);
  const isLong = item.inputText.length > SUBMISSION_PREVIEW_LENGTH;

  return (
    <div className="cv-feedback-entry">
      <div className="cv-feedback-entry-header">
        <span className="cv-feedback-date">
          {formatDate(item.createdAt)}
          {item.detailed && (
            <span className="badge" style={{ marginLeft: 8 }}>
              Detailed
            </span>
          )}
        </span>
        <button
          type="button"
          className="btn btn-danger btn-small"
          onClick={() => onDelete(item.cvFeedbackId)}
        >
          Delete
        </button>
      </div>

      <button
        type="button"
        className="cv-feedback-toggle"
        onClick={() => setShowSubmission((prev) => !prev)}
        aria-expanded={showSubmission}
      >
        {showSubmission ? '▾' : '▸'} Your submission
        {!showSubmission && isLong ? ` — ${item.inputText.slice(0, SUBMISSION_PREVIEW_LENGTH)}…` : ''}
      </button>
      {showSubmission && <p className="cv-feedback-submission">{item.inputText}</p>}

      <div className="cv-feedback-result">
        <span className="cv-feedback-result-label">AI feedback</span>
        <AiMarkdown>{item.feedbackText}</AiMarkdown>
      </div>
    </div>
  );
}
