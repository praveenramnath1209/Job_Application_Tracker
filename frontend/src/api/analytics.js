import apiClient from './axios';

export const analyticsApi = {
  getSummary: () => apiClient.get('/analytics/summary'),
};
