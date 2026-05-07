import { UserPlus } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import ErrorAlert from '../../components/ErrorAlert.jsx';
import { getApiErrorMessage } from '../../services/api.js';
import { useAuth } from '../../services/AuthContext.jsx';

export default function RegisterPage() {
  const { registerStudent, registerMentor } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ role: 'STUDENT', name: '', email: '', password: '', expertise: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (form.role === 'MENTOR') {
        await registerMentor(form);
        navigate('/mentor');
      } else {
        await registerStudent(form);
        navigate('/student');
      }
    } catch (exception) {
      setError(getApiErrorMessage(exception, 'Registration failed.'));
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-6">
      <div className="w-full max-w-md rounded-lg border border-slate-200 bg-white p-8 shadow-dashboard">
        <div className="mb-6">
          <p className="text-sm font-semibold text-primary">DoubtFlow AI</p>
          <h1 className="mt-2 text-2xl font-bold text-slate-950">Create account</h1>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <ErrorAlert message={error} />

          <div>
            <label className="field-label" htmlFor="role">Account type</label>
            <select
              id="role"
              className="field-input mt-1"
              value={form.role}
              onChange={(event) => setForm({ ...form, role: event.target.value })}
            >
              <option value="STUDENT">Student</option>
              <option value="MENTOR">Mentor</option>
            </select>
          </div>

          <div>
            <label className="field-label" htmlFor="name">Name</label>
            <input
              id="name"
              className="field-input mt-1"
              value={form.name}
              onChange={(event) => setForm({ ...form, name: event.target.value })}
              required
            />
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
              minLength={6}
              value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })}
              required
            />
          </div>

          {form.role === 'MENTOR' && (
            <div>
              <label className="field-label" htmlFor="expertise">Expertise</label>
              <input
                id="expertise"
                className="field-input mt-1"
                value={form.expertise}
                onChange={(event) => setForm({ ...form, expertise: event.target.value })}
                placeholder="Java debugging"
                required
              />
            </div>
          )}

          <button type="submit" className="primary-button w-full" disabled={loading}>
            <UserPlus size={17} />
            {loading ? 'Creating account...' : 'Create account'}
          </button>
        </form>

        <p className="mt-5 text-center text-sm text-slate-500">
          Already registered?{' '}
          <Link to="/login" className="font-semibold text-primary hover:text-blue-700">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  );
}
