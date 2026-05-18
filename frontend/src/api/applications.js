import apiClient from './axios';

export const applicationsApi = {
  getAll: () => apiClient.get('/applications/all'),
  getPaginated: (page = 0, size = 20) => apiClient.get(`/applications?page=${page}&size=${size}`),
  getById: (id) => apiClient.get(`/applications/${id}`),
  create: (data) => apiClient.post('/applications', data),
  update: (id, data) => apiClient.put(`/applications/${id}`, data),
  updateStatus: (id, status) => apiClient.patch(`/applications/${id}/status`, { status }),
  delete: (id) => apiClient.delete(`/applications/${id}`),
};
