import { useState } from 'react';

export default function InterviewQuestionCard({ q }) {
  const [selectedOptionId, setSelectedOptionId] = useState(null);
  const hasOptions = Boolean(q.options?.length);
  const answered = selectedOptionId !== null;

  return (
    <div className="card">
      <span className="badge">{q.category}</span>
      <span className={`badge difficulty-${q.difficulty?.toLowerCase()}`}>{q.difficulty}</span>
      <h3>{q.questionText}</h3>

      {hasOptions && (
        <div className="mcq-options">
          {q.options.map((opt) => {
            let className = 'mcq-option';
            if (answered && opt.correct) className += ' mcq-option-correct';
            else if (answered && opt.optionId === selectedOptionId) className += ' mcq-option-incorrect';
            return (
              <button
                type="button"
                key={opt.optionId}
                className={className}
                disabled={answered}
                onClick={() => setSelectedOptionId(opt.optionId)}
              >
                {opt.optionText}
              </button>
            );
          })}
        </div>
      )}

      {(!hasOptions || answered) && q.guidanceText && <p>{q.guidanceText}</p>}
    </div>
  );
}
