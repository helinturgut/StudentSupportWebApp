import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { UiPreferencesProvider } from './context/UiPreferencesContext';
import ProtectedRoute from './components/ProtectedRoute';
import Layout from './components/Layout';

import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import ResetPasswordPage from './pages/ResetPasswordPage';
import DashboardPage from './pages/DashboardPage';
import ChatbotPage from './pages/ChatbotPage';
import CvGuidancePage from './pages/CvGuidancePage';
import RemindersPage from './pages/RemindersPage';
import ResourcesPage from './pages/ResourcesPage';
import AdminResourcesPage from './pages/AdminResourcesPage';
import InterviewQuestionsPage from './pages/InterviewQuestionsPage';
import AdminInterviewQuestionsPage from './pages/AdminInterviewQuestionsPage';
import CareerPathwaysPage from './pages/CareerPathwaysPage';
import CareerPathwayDetailPage from './pages/CareerPathwayDetailPage';
import AdminCareerPathwaysPage from './pages/AdminCareerPathwaysPage';
import FeedbackPage from './pages/FeedbackPage';
import SettingsPage from './pages/SettingsPage';
import AdminFeedbackPage from './pages/AdminFeedbackPage';
import AdminAnalyticsPage from './pages/AdminAnalyticsPage';
import AdminStaffAccountsPage from './pages/AdminStaffAccountsPage';

function HomePage() {
  const { user, isAdmin, isStaff } = useAuth();
  if (!user) return <LandingPage />;
  return <Navigate to={isAdmin || isStaff ? '/admin/analytics' : '/dashboard'} replace />;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route path="/reset-password" element={<ResetPasswordPage />} />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/chatbot" element={<ChatbotPage />} />
          <Route path="/cv-guidance" element={<CvGuidancePage />} />
          <Route path="/resources" element={<ResourcesPage />} />
          <Route path="/interview-questions" element={<InterviewQuestionsPage />} />
          <Route path="/career-pathways" element={<CareerPathwaysPage />} />
          <Route path="/career-pathways/:pathwayId" element={<CareerPathwayDetailPage />} />
          <Route path="/reminders" element={<RemindersPage />} />
          <Route path="/feedback" element={<FeedbackPage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute roles={['ADMIN', 'CAREER_SUPPORT_STAFF']} />}>
        <Route element={<Layout />}>
          <Route path="/admin/resources" element={<AdminResourcesPage />} />
          <Route path="/admin/interview-questions" element={<AdminInterviewQuestionsPage />} />
          <Route path="/admin/career-pathways" element={<AdminCareerPathwaysPage />} />
          <Route path="/admin/feedback" element={<AdminFeedbackPage />} />
          <Route path="/admin/analytics" element={<AdminAnalyticsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute requireAdmin />}>
        <Route element={<Layout />}>
          <Route path="/admin/staff-accounts" element={<AdminStaffAccountsPage />} />
        </Route>
      </Route>

      <Route path="/" element={<HomePage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <UiPreferencesProvider>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </UiPreferencesProvider>
    </BrowserRouter>
  );
}
