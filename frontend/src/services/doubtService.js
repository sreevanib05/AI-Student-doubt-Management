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
  openAttachment(id) {
    const previewWindow = window.open('', '_blank', 'noopener,noreferrer');

    return api.get(`/doubts/${id}/attachment`, { responseType: 'blob' })
      .then((response) => {
        const blob = new Blob([response.data], { type: response.headers['content-type'] || 'application/pdf' });
        const url = window.URL.createObjectURL(blob);

        if (previewWindow) {
          previewWindow.location.href = url;
        } else {
          window.open(url, '_blank', 'noopener,noreferrer');
        }

        window.setTimeout(() => window.URL.revokeObjectURL(url), 60000);
      })
      .catch((exception) => {
        previewWindow?.close();
        throw exception;
      });
  },
};
