import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { extractErrorMessage } from '../api/client';
import Alert from '../components/Alert';
import PasswordField from '../components/PasswordField';
import iconLight from '../assets/icon-light.png';
import iconDark from '../assets/icon-dark.png';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const loggedInUser = await login(email, password);
      const staffRoles = ['ADMIN', 'CAREER_SUPPORT_STAFF'];
      navigate(staffRoles.includes(loggedInUser.role) ? '/admin/analytics' : '/dashboard');
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-page auth-bg-image">
      <Link to="/" className="auth-back-link">
        <span aria-hidden="true">←</span> Back to home
      </Link>
      <form className="auth-card" onSubmit={handleSubmit}>
        <div className="auth-logo">
          <img src={iconLight} alt="" className="app-brand-logo-light" />
          <img src={iconDark} alt="" className="app-brand-logo-dark" />
        </div>
        <h1>Log in</h1>
        <p className="auth-subtitle">Student Support &amp; Career Advisor</p>
        <Alert>{error}</Alert>
        <label className="field">
          <span>Email</span>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <PasswordField label="Password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <p className="auth-switch">
          <Link to="/forgot-password">Forgot your password?</Link>
        </p>
        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? 'Logging in…' : 'Log in'}
        </button>
        <p className="auth-switch">
          Don&apos;t have an account? <Link to="/register">Register</Link>
        </p>
      </form>
    </div>
  );
}
