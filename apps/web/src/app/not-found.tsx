export default function NotFound() {
  return (
    <div className="flex items-center justify-center min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 dark:from-gray-900 dark:to-gray-800">
      <div className="text-center">
        <div className="text-8xl mb-4">🍞</div>
        <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">404</h1>
        <p className="text-gray-600 dark:text-gray-400 mb-6">페이지를 찾을 수 없습니다</p>
        <a href="/" className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          홈으로 돌아가기
        </a>
      </div>
    </div>
  );
}
