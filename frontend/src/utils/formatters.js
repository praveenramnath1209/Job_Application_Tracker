export const formatDate = (dateString) => {
  if (!dateString) return '—';
  return new Date(dateString).toLocaleDateString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric'
  });
};

export const formatDateTime = (dateTimeString) => {
  if (!dateTimeString) return '—';
  return new Date(dateTimeString).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
};

export const formatCTC = (ctc) => {
  if (!ctc) return '—';
  return `₹${Number(ctc).toFixed(1)} LPA`;
};

export const timeAgo = (dateTimeString) => {
  if (!dateTimeString) return '';
  const now = new Date();
  const date = new Date(dateTimeString);
  const diffMs = now - date;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffSec < 60) return 'Just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  if (diffHour < 24) return `${diffHour}h ago`;
  if (diffDay < 7) return `${diffDay}d ago`;
  return formatDate(dateTimeString);
};

export const getErrorMessage = (error) => {
  if (error?.response?.data?.message) return error.response.data.message;
  if (error?.response?.data?.errors?.length) return error.response.data.errors[0];
  if (error?.message) return error.message;
  return 'An unexpected error occurred';
};
