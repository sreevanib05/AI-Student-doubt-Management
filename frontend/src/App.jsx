import { Navigate, Route, Routes } from 'react-router-dom';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import DashboardLayout from './layouts/DashboardLayout.jsx';
import { useAuth } from './services/AuthContext.jsx';
import LoginPage from './pages/auth/LoginPage.jsx';
import RegisterPage from './pages/auth/RegisterPage.jsx';
import StudentDashboard from './pages/student/StudentDashboard.jsx';
import SubmitDoubtPage from './pages/student/SubmitDoubtPage.jsx';
import MyDoubtsPage from './pages/student/MyDoubtsPage.jsx';
import MentorDashboard from './pages/mentor/MentorDashboard.jsx';
import AssignedDoubtsPage from './pages/mentor/AssignedDoubtsPage.jsx';
import AdminDashboard from './pages/admin/AdminDashboard.jsx';
import AdminAnalyticsPage from './pages/admin/AdminAnalyticsPage.jsx';
import MentorManagementPage from './pages/admin/MentorManagementPage.jsx';

function HomeRedirect() {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  const landing = {
    STUDENT: '/student',
    MENTOR: '/mentor',
    ADMIN: '/admin',
  };

  return <Navigate to={landing[user.role]} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomeRedirect />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route
        path="/student"
        element={
          <ProtectedRoute allowedRoles={['STUDENT']}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<StudentDashboard />} />
        <Route path="submit" element={<SubmitDoubtPage />} />
        <Route path="doubts" element={<MyDoubtsPage />} />
      </Route>

      <Route
        path="/mentor"
        element={
          <ProtectedRoute allowedRoles={['MENTOR']}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<MentorDashboard />} />
        <Route path="assigned" element={<AssignedDoubtsPage />} />
      </Route>

      <Route
        path="/admin"
        element={
          <ProtectedRoute allowedRoles={['ADMIN']}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<AdminDashboard />} />
        <Route path="analytics" element={<AdminAnalyticsPage />} />
        <Route path="mentors" element={<MentorManagementPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
