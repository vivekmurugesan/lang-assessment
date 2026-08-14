import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import './App.css';

// Auth
import LoginPage from './pages/auth/LoginPage';
import CandidateLoginPage from './pages/auth/CandidateLoginPage';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import AssessmentSetup from './pages/admin/AssessmentSetup';
import CandidateOnboarding from './pages/admin/CandidateOnboarding';
import AssessmentMonitoring from './pages/admin/AssessmentMonitoring';
import ReportingDashboard from './pages/admin/ReportingDashboard';
import EvaluationReview from './pages/admin/EvaluationReview';

// Candidate Pages
import CandidateHome from './pages/candidate/CandidateHome';
import AssessmentTaking from './pages/candidate/AssessmentTaking';

// Common Components
import PrivateRoute from './components/PrivateRoute';
import { useAuthStore } from './store/authStore';

function App() {
  const { checkAuth } = useAuthStore();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          {/* Auth Routes */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/candidate-login" element={<CandidateLoginPage />} />

          {/* Admin Routes */}
          <Route
            path="/admin/*"
            element={
              <PrivateRoute role="ADMIN">
                <Routes>
                  <Route path="/" element={<AdminDashboard />} />
                  <Route path="/assessments" element={<AssessmentSetup />} />
                  <Route path="/onboarding" element={<CandidateOnboarding />} />
                  <Route path="/monitoring" element={<AssessmentMonitoring />} />
                  <Route path="/reports" element={<ReportingDashboard />} />
                  <Route path="/evaluation" element={<EvaluationReview />} />
                </Routes>
              </PrivateRoute>
            }
          />

          {/* Candidate Routes */}
          <Route
            path="/candidate/*"
            element={
              <PrivateRoute role="CANDIDATE">
                <Routes>
                  <Route path="/" element={<CandidateHome />} />
                  <Route path="/assessment/:id" element={<AssessmentTaking />} />
                </Routes>
              </PrivateRoute>
            }
          />

          {/* Default Route */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>

        <ToastContainer
          position="top-right"
          autoClose={3000}
          hideProgressBar={false}
          newestOnTop
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        />
      </div>
    </Router>
  );
}

export default App;
