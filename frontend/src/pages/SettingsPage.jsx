import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAccount, updateFullName, changePassword, deleteAccount } from '../api/account';
import { getProfile, updateProfile } from '../api/profile';
import { extractErrorMessage } from '../api/client';
import { useAuth } from '../context/AuthContext';
import { useUiPreferences } from '../context/UiPreferencesContext';
import Alert from '../components/Alert';

const THEME_OPTIONS = [
  { value: 'system', label: 'Match system' },
  { value: 'light', label: 'Light' },
  { value: 'dark', label: 'Night mode' },
];

const TEXT_SIZE_OPTIONS = [
  { value: 'small', label: 'Small' },
  { value: 'medium', label: 'Medium' },
  { value: 'large', label: 'Large' },
];

export default function SettingsPage() {
  const { user, updateUser, logout } = useAuth();
  const isStudent = user?.role === 'STUDENT';
  const { theme, setTheme, textSize, setTextSize } = useUiPreferences();
  const navigate = useNavigate();

  const [email, setEmail] = useState('');
  const [fullName, setFullName] = useState('');
  const [accountError, setAccountError] = useState('');
  const [accountSuccess, setAccountSuccess] = useState('');
  const [savingAccount, setSavingAccount] = useState(false);

  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [passwordError, setPasswordError] = useState('');
  const [passwordSuccess, setPasswordSuccess] = useState('');
  const [savingPassword, setSavingPassword] = useState(false);

  const [profile, setProfile] = useState({
    course: '',
    previousBackground: '',
    careerGoal: '',
    skillInterests: '',
  });
  const [profileError, setProfileError] = useState('');
  const [profileSuccess, setProfileSuccess] = useState('');
  const [savingProfile, setSavingProfile] = useState(false);
  const [loading, setLoading] = useState(true);

  const [showDeleteForm, setShowDeleteForm] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [deleteError, setDeleteError] = useState('');
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    Promise.all([getAccount(), isStudent ? getProfile() : Promise.resolve(null)])
      .then(([account, studentProfile]) => {
        setEmail(account.email);
        setFullName(account.fullName);
        if (studentProfile) {
          setProfile({
            course: studentProfile.course || '',
            previousBackground: studentProfile.previousBackground || '',
            careerGoal: studentProfile.careerGoal || '',
            skillInterests: studentProfile.skillInterests || '',
          });
        }
      })
      .catch((err) => setAccountError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [isStudent]);

  const handleAccountSubmit = (e) => {
    e.preventDefault();
    setAccountError('');
    setAccountSuccess('');
    setSavingAccount(true);
    updateFullName(fullName.trim())
      .then((account) => {
        updateUser({ fullName: account.fullName });
        setAccountSuccess('Your name has been updated.');
      })
      .catch((err) => setAccountError(extractErrorMessage(err)))
      .finally(() => setSavingAccount(false));
  };

  const handlePasswordSubmit = (e) => {
    e.preventDefault();
    setPasswordError('');
    setPasswordSuccess('');
    if (newPassword !== confirmPassword) {
      setPasswordError('New password and confirmation do not match.');
      return;
    }
    setSavingPassword(true);
    changePassword(currentPassword, newPassword)
      .then(() => {
        setPasswordSuccess('Your password has been changed.');
        setCurrentPassword('');
        setNewPassword('');
        setConfirmPassword('');
      })
      .catch((err) => setPasswordError(extractErrorMessage(err)))
      .finally(() => setSavingPassword(false));
  };

  const handleProfileSubmit = (e) => {
    e.preventDefault();
    setProfileError('');
    setProfileSuccess('');
    setSavingProfile(true);
    updateProfile(profile)
      .then(() => setProfileSuccess('Your profile has been updated.'))
      .catch((err) => setProfileError(extractErrorMessage(err)))
      .finally(() => setSavingProfile(false));
  };

  const handleDeleteAccount = (e) => {
    e.preventDefault();
    setDeleteError('');
    if (!window.confirm('This will permanently delete your account and all its data. Are you sure?')) {
      return;
    }
    setDeleting(true);
    deleteAccount(deletePassword)
      .then(() => {
        logout();
        navigate('/login');
      })
      .catch((err) => setDeleteError(extractErrorMessage(err)))
      .finally(() => setDeleting(false));
  };

  if (loading) {
    return (
      <div className="page-shell page-settings">
        <h1>Settings</h1>
        <p>Loading…</p>
      </div>
    );
  }

  return (
    <div className="page-shell page-settings">
      <h1>Settings</h1>

      <h2>Appearance</h2>
      <div className="admin-form card">
        <label className="field">
          <span>Theme</span>
          <select value={theme} onChange={(e) => setTheme(e.target.value)}>
            {THEME_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          <span>Text size</span>
          <select value={textSize} onChange={(e) => setTextSize(e.target.value)}>
            {TEXT_SIZE_OPTIONS.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </label>
        <p className="card-meta">Changes apply immediately and are remembered on this device.</p>
      </div>

      <h2>Account</h2>
      <Alert>{accountError}</Alert>
      <Alert type="success">{accountSuccess}</Alert>
      <form className="admin-form card" onSubmit={handleAccountSubmit}>
        <label className="field">
          <span>Email</span>
          <input type="email" value={email} disabled />
        </label>
        <label className="field">
          <span>Full name</span>
          <input
            type="text"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />
        </label>
        <button type="submit" className="btn btn-primary" disabled={savingAccount}>
          Save name
        </button>
      </form>

      <h2>Change password</h2>
      <Alert>{passwordError}</Alert>
      <Alert type="success">{passwordSuccess}</Alert>
      <form className="admin-form card" onSubmit={handlePasswordSubmit}>
        <label className="field">
          <span>Current password</span>
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            required
          />
        </label>
        <label className="field">
          <span>New password</span>
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            minLength={8}
            required
          />
        </label>
        <label className="field">
          <span>Confirm new password</span>
          <input
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            minLength={8}
            required
          />
        </label>
        <button type="submit" className="btn btn-primary" disabled={savingPassword}>
          Change password
        </button>
      </form>

      {isStudent && (
        <>
          <h2>Academic profile</h2>
          <Alert>{profileError}</Alert>
          <Alert type="success">{profileSuccess}</Alert>
          <form className="admin-form card" onSubmit={handleProfileSubmit}>
            <label className="field">
              <span>Course</span>
              <input
                type="text"
                value={profile.course}
                onChange={(e) => setProfile((p) => ({ ...p, course: e.target.value }))}
              />
            </label>
            <label className="field">
              <span>Previous background</span>
              <textarea
                value={profile.previousBackground}
                onChange={(e) => setProfile((p) => ({ ...p, previousBackground: e.target.value }))}
              />
            </label>
            <label className="field">
              <span>Career goal</span>
              <input
                type="text"
                value={profile.careerGoal}
                onChange={(e) => setProfile((p) => ({ ...p, careerGoal: e.target.value }))}
              />
            </label>
            <label className="field">
              <span>Skill interests</span>
              <input
                type="text"
                value={profile.skillInterests}
                onChange={(e) => setProfile((p) => ({ ...p, skillInterests: e.target.value }))}
              />
            </label>
            <button type="submit" className="btn btn-primary" disabled={savingProfile}>
              Save profile
            </button>
          </form>
        </>
      )}

      <h2>Danger zone</h2>
      <Alert>{deleteError}</Alert>
      <div className="admin-form card">
        <p>Deleting your account permanently removes it and signs you out. This cannot be undone.</p>
        {!showDeleteForm ? (
          <button type="button" className="btn btn-danger" onClick={() => setShowDeleteForm(true)}>
            Delete my account
          </button>
        ) : (
          <form onSubmit={handleDeleteAccount}>
            <label className="field">
              <span>Confirm your password</span>
              <input
                type="password"
                value={deletePassword}
                onChange={(e) => setDeletePassword(e.target.value)}
                required
              />
            </label>
            <div className="form-actions">
              <button type="submit" className="btn btn-danger" disabled={deleting}>
                {deleting ? 'Deleting…' : 'Confirm delete'}
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => {
                  setShowDeleteForm(false);
                  setDeletePassword('');
                  setDeleteError('');
                }}
              >
                Cancel
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
