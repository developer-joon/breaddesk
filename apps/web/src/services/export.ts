import api from '@/lib/api';

export async function exportInquiries(): Promise<void> {
  // 기본: 최근 30일
  const to = new Date().toISOString().split('T')[0];
  const from = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  const { data } = await api.get('/export/inquiries', {
    params: { from, to },
    responseType: 'blob',
  });
  
  const blob = new Blob([data], { type: 'text/csv; charset=utf-8' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `inquiries-${from}_${to}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

export async function exportTasks(): Promise<void> {
  // 기본: 최근 30일
  const to = new Date().toISOString().split('T')[0];
  const from = new Date(Date.now() - 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

  const { data } = await api.get('/export/tasks', {
    params: { from, to },
    responseType: 'blob',
  });
  
  const blob = new Blob([data], { type: 'text/csv; charset=utf-8' });
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = `tasks-${from}_${to}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}
