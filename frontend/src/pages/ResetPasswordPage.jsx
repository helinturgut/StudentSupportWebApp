import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { resetPassword } from '../api/auth';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';
import PasswordField from '../components/PasswordField';

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get('token') || '';
  const isWelcome = searchParams.get('welcome') === 'true';
  const navigate = useNavigate();

  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (newPassword !== confirmPassword) {
      setError('New password and confirmation do not match.');
      return;
    }
    setSubmitting(true);
    try {
      await resetPassword(token, newPassword);
      navigate('/login');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page auth-bg-image">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h1>{isWelcome ? 'Create your password' : 'Reset password'}</h1>
        <p className="auth-subtitle">
          {isWelcome
            ? 'Set a password to activate your staff account.'
            : 'Student Support & Career Advisor'}
        </p>
        <Alert>{error}</Alert>
        {!token ? (
          <Alert>This link is missing its token. Please use the link from your email.</Alert>
        ) : (
          <>
            <PasswordField
              label="New password"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              minLength={8}
              hint="At least 8 characters"
            />
            <PasswordField
              label="Confirm new password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              minLength={8}
            />
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? 'Saving…' : isWelcome ? 'Create password' : 'Reset password'}
            </button>
          </>
        )}
        <p className="auth-switch">
          <Link to="/login">Back to log in</Link>
        </p>
      </form>
    </div>
  );
}
