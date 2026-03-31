import api from '@/lib/api';
import type { ApiResponse, SearchResult } from '@/types';

export async function search(keyword: string): Promise<SearchResult> {
  const { data } = await api.get<ApiResponse<SearchResult>>('/search', {
    params: { keyword },
  });
  return data.data;
}
