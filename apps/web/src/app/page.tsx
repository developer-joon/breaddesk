'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/stores/auth';

export default function Home() {
  const router = useRouter();
  const { isAuthenticated, isLoading, checkAuth } = useAuthStore();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  useEffect(() => {
    if (!isLoading && isAuthenticated) {
      router.push('/dashboard');
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800">
        <div className="text-center">
          <div className="text-6xl mb-4">🍞</div>
          <div className="text-gray-600 dark:text-gray-400">로딩 중...</div>
        </div>
      </div>
    );
  }

  if (isAuthenticated) {
    return null; // Will redirect
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-indigo-50 to-purple-50 dark:from-gray-900 dark:via-gray-800 dark:to-gray-900">
      {/* Hero Section */}
      <div className="relative overflow-hidden">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 sm:py-20">
          <div className="text-center">
            {/* Logo */}
            <div className="flex justify-center mb-6">
              <div className="text-7xl sm:text-8xl">🍞</div>
            </div>

            {/* Title */}
            <h1 className="text-4xl sm:text-5xl md:text-6xl font-bold text-gray-900 dark:text-white mb-4 sm:mb-6">
              BreadDesk
            </h1>
            <p className="text-xl sm:text-2xl text-gray-600 dark:text-gray-300 mb-6 sm:mb-8 max-w-2xl mx-auto px-4">
              AI 기반 서비스 데스크 + 업무 관리 플랫폼
            </p>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center items-center px-4">
              <Link
                href="/login"
                className="w-full sm:w-auto px-8 py-3 bg-blue-600 hover:bg-blue-700 dark:bg-blue-500 dark:hover:bg-blue-600 text-white font-semibold rounded-lg shadow-lg transition-all transform hover:scale-105"
              >
                로그인
              </Link>
              <a
                href="#features"
                className="w-full sm:w-auto px-8 py-3 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-900 dark:text-white font-semibold rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 transition-all"
              >
                더 알아보기
              </a>
            </div>
          </div>
        </div>

        {/* Decorative Elements */}
        <div className="absolute top-0 left-0 w-full h-full overflow-hidden -z-10">
          <div className="absolute top-20 left-10 w-72 h-72 bg-blue-300 dark:bg-blue-900 rounded-full mix-blend-multiply dark:mix-blend-soft-light filter blur-xl opacity-20 animate-blob"></div>
          <div className="absolute top-40 right-10 w-72 h-72 bg-purple-300 dark:bg-purple-900 rounded-full mix-blend-multiply dark:mix-blend-soft-light filter blur-xl opacity-20 animate-blob animation-delay-2000"></div>
          <div className="absolute -bottom-8 left-20 w-72 h-72 bg-pink-300 dark:bg-pink-900 rounded-full mix-blend-multiply dark:mix-blend-soft-light filter blur-xl opacity-20 animate-blob animation-delay-4000"></div>
        </div>
      </div>

      {/* Features Section */}
      <div id="features" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 sm:py-20">
        <h2 className="text-3xl sm:text-4xl font-bold text-center text-gray-900 dark:text-white mb-8 sm:mb-12">
          주요 기능
        </h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 sm:gap-8">
          {/* Feature 1 */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 sm:p-8 shadow-lg border border-gray-100 dark:border-gray-700 hover:shadow-xl transition-shadow">
            <div className="text-4xl sm:text-5xl mb-4">🤖</div>
            <h3 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3">AI 자동 응답</h3>
            <p className="text-gray-600 dark:text-gray-300">
              고객 문의에 AI가 자동으로 답변하고, 필요시 담당자에게 에스컬레이션합니다.
            </p>
          </div>

          {/* Feature 2 */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 sm:p-8 shadow-lg border border-gray-100 dark:border-gray-700 hover:shadow-xl transition-shadow">
            <div className="text-4xl sm:text-5xl mb-4">📋</div>
            <h3 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3">업무 관리</h3>
            <p className="text-gray-600 dark:text-gray-300">
              칸반 보드로 업무를 시각화하고, 팀 협업을 효율적으로 관리합니다.
            </p>
          </div>

          {/* Feature 3 */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 sm:p-8 shadow-lg border border-gray-100 dark:border-gray-700 hover:shadow-xl transition-shadow">
            <div className="text-4xl sm:text-5xl mb-4">⏱️</div>
            <h3 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3">SLA 관리</h3>
            <p className="text-gray-600 dark:text-gray-300">
              응답 시간 및 해결 시간을 추적하고, SLA 준수율을 모니터링합니다.
            </p>
          </div>

          {/* Feature 4 */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 sm:p-8 shadow-lg border border-gray-100 dark:border-gray-700 hover:shadow-xl transition-shadow">
            <div className="text-4xl sm:text-5xl mb-4">📊</div>
            <h3 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3">실시간 대시보드</h3>
            <p className="text-gray-600 dark:text-gray-300">
              팀 현황과 주요 지표를 한눈에 확인할 수 있는 대시보드를 제공합니다.
            </p>
          </div>

          {/* Feature 5 */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 sm:p-8 shadow-lg border border-gray-100 dark:border-gray-700 hover:shadow-xl transition-shadow">
            <div className="text-4xl sm:text-5xl mb-4">📚</div>
            <h3 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3">지식 베이스</h3>
            <p className="text-gray-600 dark:text-gray-300">
              자주 묻는 질문과 솔루션을 체계적으로 관리하고 공유합니다.
            </p>
          </div>

          {/* Feature 6 */}
          <div className="bg-white dark:bg-gray-800 rounded-xl p-6 sm:p-8 shadow-lg border border-gray-100 dark:border-gray-700 hover:shadow-xl transition-shadow">
            <div className="text-4xl sm:text-5xl mb-4">🔔</div>
            <h3 className="text-xl sm:text-2xl font-bold text-gray-900 dark:text-white mb-3">실시간 알림</h3>
            <p className="text-gray-600 dark:text-gray-300">
              중요한 이벤트와 업데이트를 실시간으로 받아볼 수 있습니다.
            </p>
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="border-t border-gray-200 dark:border-gray-800 py-8 mt-12 sm:mt-20">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <p className="text-gray-600 dark:text-gray-400">
            © 2025 BreadDesk. Made with 🍞 and ❤️
          </p>
        </div>
      </footer>
    </div>
  );
}
