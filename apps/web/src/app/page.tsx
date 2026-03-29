'use client';

import { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../components/ui/card';
import { Badge } from '../components/ui/badge';
import { Skeleton } from '../components/ui/skeleton';
import { StatCard } from '../components/StatCard';
import { api } from '../lib/api';
import type { DashboardStats, Inquiry } from '../types';
import { Inbox, ClipboardList, TrendingUp, CheckCircle, AlertCircle } from 'lucide-react';
import Link from 'next/link';

export default function DashboardPage() {
  const [stats, setStats] = useState<DashboardStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchStats = async () => {
      setLoading(true);
      setError(null);
      const response = await api.getDashboardStats();
      if (response.success && response.data) {
        setStats(response.data);
      } else {
        setError(response.error || '통계를 불러오는데 실패했습니다.');
      }
      setLoading(false);
    };

    fetchStats();
  }, []);

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
          {[...Array(4)].map((_, i) => (
            <Card key={i}>
              <CardHeader>
                <Skeleton className="h-4 w-24" />
              </CardHeader>
              <CardContent>
                <Skeleton className="h-8 w-16" />
              </CardContent>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex h-full items-center justify-center">
        <Card className="w-full max-w-md">
          <CardHeader>
            <div className="flex items-center gap-2">
              <AlertCircle className="h-5 w-5 text-destructive" />
              <CardTitle>오류 발생</CardTitle>
            </div>
          </CardHeader>
          <CardContent>
            <p className="text-sm text-muted-foreground">{error}</p>
          </CardContent>
        </Card>
      </div>
    );
  }

  if (!stats) return null;

  const statusLabels: Record<string, string> = {
    WAITING: '대기 중',
    IN_PROGRESS: '진행 중',
    REVIEW: '검토 중',
    DONE: '완료',
  };

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">대시보드</h1>
        <p className="text-muted-foreground">BreadDesk 전체 현황을 확인하세요</p>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <StatCard
          title="전체 문의"
          value={stats.totalInquiries}
          icon={Inbox}
          description="누적 문의 수"
        />
        <StatCard
          title="미해결 문의"
          value={stats.unresolvedInquiries}
          icon={AlertCircle}
          description="처리 필요"
        />
        <StatCard
          title="오늘 문의"
          value={stats.todayInquiries}
          icon={TrendingUp}
          description="금일 접수"
        />
        <StatCard
          title="해결률"
          value={`${stats.resolvedRate.toFixed(1)}%`}
          icon={CheckCircle}
          description="전체 평균"
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <Card>
          <CardHeader>
            <CardTitle>업무 현황</CardTitle>
            <CardDescription>상태별 업무 분포</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {Object.entries(stats.tasksByStatus).map(([status, count]) => (
                <div key={status} className="flex items-center justify-between">
                  <span className="text-sm font-medium">
                    {statusLabels[status] || status}
                  </span>
                  <Badge variant="secondary">{count}</Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>최근 문의</CardTitle>
            <CardDescription>최근 접수된 문의 내역</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {stats.recentInquiries.length === 0 ? (
                <p className="text-sm text-muted-foreground">최근 문의가 없습니다.</p>
              ) : (
                stats.recentInquiries.map((inquiry: Inquiry) => (
                  <Link
                    key={inquiry.id}
                    href={`/inquiries/${inquiry.id}`}
                    className="block rounded-lg border p-3 transition-colors hover:bg-muted/50"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <p className="text-sm font-medium">{inquiry.senderName}</p>
                        <p className="mt-1 text-xs text-muted-foreground line-clamp-2">
                          {inquiry.message}
                        </p>
                      </div>
                      <Badge variant="outline" className="ml-2">
                        {inquiry.channel}
                      </Badge>
                    </div>
                    <div className="mt-2 flex items-center justify-between">
                      <span className="text-xs text-muted-foreground">
                        {new Date(inquiry.createdAt).toLocaleString('ko-KR')}
                      </span>
                      <Badge
                        variant={
                          inquiry.status === 'RESOLVED' || inquiry.status === 'CLOSED'
                            ? 'default'
                            : 'secondary'
                        }
                      >
                        {inquiry.status}
                      </Badge>
                    </div>
                  </Link>
                ))
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
