import apiClient from './axios';

export const interviewsApi = {
  getByApplication: (applicationId) => apiClient.get(`/applications/${applicationId}/interviews`),
  add: (applicationId, data) => apiClient.post(`/applications/${applicationId}/interviews`, data),
  delete: (applicationId, roundId) => apiClient.delete(`/applications/${applicationId}/interviews/${roundId}`),
};
