import { createContext, useContext, useMemo, useState } from 'react';
import api from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('doubtflow_user');
    return savedUser ? JSON.parse(savedUser) : null;
  });

  async function login({ email, password, role }) {
    const response = await api.post('/auth/login', { email, password, role });
    saveSession(response.data);
    return response.data;
  }

  async function registerStudent({ name, email, password }) {
    const response = await api.post('/auth/student/register', { name, email, password });
    saveSession(response.data);
    return response.data;
  }

  async function registerMentor({ name, email, password, expertise }) {
    const response = await api.post('/auth/mentor/register', { name, email, password, expertise });
    saveSession(response.data);
    return response.data;
  }

  function saveSession(data) {
    const sessionUser = {
      id: data.id,
      name: data.name,
      email: data.email,
      role: data.role,
    };

    localStorage.setItem('doubtflow_token', data.token);
    localStorage.setItem('doubtflow_user', JSON.stringify(sessionUser));
    setUser(sessionUser);
  }

  function logout() {
    localStorage.removeItem('doubtflow_token');
    localStorage.removeItem('doubtflow_user');
    setUser(null);
  }

  const value = useMemo(() => ({ user, login, registerStudent, registerMentor, logout }), [user]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
