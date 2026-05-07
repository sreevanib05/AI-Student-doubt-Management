import api from './api';

export const doubtService = {
  create(payload) {
    return api.post('/doubts/create', payload).then((response) => response.data);
  },
  myDoubts() {
    return api.get('/doubts/my').then((response) => response.data);
  },
  assigned() {
    return api.get('/doubts/assigned').then((response) => response.data);
  },
  all() {
    return api.get('/doubts/all').then((response) => response.data);
  },
  byCategory(category) {
    return api.get(`/doubts/category/${category}`).then((response) => response.data);
  },
  updateStatus(id, status) {
    return api.patch(`/doubts/${id}/status`, { status }).then((response) => response.data);
  },
  suggestions(text) {
    return api.post('/doubts/suggestions', { text }).then((response) => response.data);
  },
};
