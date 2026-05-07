import api from './api';

export const mentorService = {
  all() {
    return api.get('/mentors/all').then((response) => response.data);
  },
  respond(payload) {
    return api.post('/mentors/respond', payload).then((response) => response.data);
  },
};
