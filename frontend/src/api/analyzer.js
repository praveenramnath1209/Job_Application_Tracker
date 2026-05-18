import apiClient from './axios';

export const analyzerApi = {
  analyze: (data) => apiClient.post('/analyzer/analyze', data),
  analyzePdf: (formData) => apiClient.post('/analyzer/analyze-pdf', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }),
};
