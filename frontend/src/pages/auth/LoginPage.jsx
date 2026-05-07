import { LogIn } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import { getApiErrorMessage } from '../../services/api.js';
import { useAuth } from '../../services/AuthContext.jsx';

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '', role: 'STUDENT' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const session = await login(form);
      const route = session.role === 'STUDENT' ? '/student' : session.role === 'MENTOR' ? '/mentor' : '/admin';
      navigate(route);
    } catch (exception) {
      setError(getApiErrorMessage(exception, 'Login failed.'));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-6">
      <div className="w-full max-w-md rounded-lg border border-slate-200 bg-white p-8 shadow-dashboard">
        <div className="mb-6">
          <p className="text-sm font-semibold text-primary">DoubtFlow AI</p>
          <h1 className="mt-2 text-2xl font-bold text-slate-950">Sign in</h1>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <ErrorAlert message={error} />

          <div>
            <label className="field-label" htmlFor="role">Role</label>
            <select
              id="role"
              className="field-input mt-1"
              value={form.role}
              onChange={(event) => setForm({ ...form, role: event.target.value })}
            >
              <option value="STUDENT">Student</option>
              <option value="MENTOR">Mentor</option>
              <option value="ADMIN">Admin/Faculty</option>
            </select>
          </div>

          <div>
            <label className="field-label" htmlFor="email">Email</label>
            <input
              id="email"
              className="field-input mt-1"
              type="email"
              value={form.email}
              onChange={(event) => setForm({ ...form, email: event.target.value })}
              required
            />
          </div>

          <div>
            <label className="field-label" htmlFor="password">Password</label>
            <input
              id="password"
              className="field-input mt-1"
              type="password"
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              required
            />
          </div>

          <button type="submit" className="primary-button w-full" disabled={loading}>
            <LogIn size={17} />
            {loading ? 'Signing in...' : 'Sign in'}
          </button>
        </form>

        <p className="mt-5 text-center text-sm text-slate-500">
          Student account?{' '}
          <Link to="/register" className="font-semibold text-primary hover:text-blue-700">
            Register here
          </Link>
        </p>
      </div>
    </div>
  );
}
