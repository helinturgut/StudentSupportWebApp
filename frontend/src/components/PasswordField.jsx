import { useState } from 'react';

export default function PasswordField({ label, value, onChange, minLength, hint }) {
  const [visible, setVisible] = useState(false);

  return (
    <label className="field">
      <span>{label}</span>
      <div className="password-input">
        <input
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={onChange}
          minLength={minLength}
          required
        />
        <button
          type="button"
          className="btn-toggle-password"
          onClick={() => setVisible((v) => !v)}
          aria-label={visible ? 'Hide password' : 'Show password'}
        >
          {visible ? 'Hide' : 'Show'}
        </button>
      </div>
      {hint && <small>{hint}</small>}
    </label>
  );
}
