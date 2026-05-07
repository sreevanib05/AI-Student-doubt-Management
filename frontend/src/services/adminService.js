import api from './api';

export const adminService = {
  analytics() {
    return api.get('/admin/analytics').then((response) => response.data);
  },
  createMentor(payload) {
    return api.post('/admin/mentors', payload).then((response) => response.data);
  },
  assignMentor(doubtId, mentorId) {
    return api.patch(`/admin/doubts/${doubtId}/assign`, { mentorId }).then((response) => response.data);
  },
  unassignMentor(doubtId) {
    return api.patch(`/admin/doubts/${doubtId}/unassign`).then((response) => response.data);
  },
  simulateMentors() {
    return api.post('/admin/simulate-mentors').then((response) => response.data);
  },
};
