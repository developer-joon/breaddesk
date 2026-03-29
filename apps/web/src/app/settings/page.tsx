'use client';

export default function SettingsPage() {
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">설정</h1>
        <p className="text-gray-600 mt-1">시스템 설정을 관리합니다</p>
      </div>

      <div className="bg-white rounded-lg shadow p-6">
        <div className="space-y-6">
          {/* LLM 설정 */}
          <div>
            <h2 className="text-lg font-semibold text-gray-900 mb-4">LLM 설정</h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Provider
                </label>
                <select className="w-full md:w-64 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500">
                  <option>Ollama (로컬)</option>
                  <option>OpenAI</option>
                  <option>Claude</option>
                  <option>vLLM</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Model
                </label>
                <input
                  type="text"
                  defaultValue="llama3.1:8b"
                  className="w-full md:w-64 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                />
              </div>
            </div>
          </div>

          {/* 업무 유형 */}
          <div className="border-t pt-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">업무 유형</h2>
            <div className="space-y-2">
              {['DEVELOPMENT', 'ACCESS', 'INFRA', 'FIREWALL', 'DEPLOY', 'INCIDENT', 'GENERAL'].map(type => (
                <div key={type} className="flex items-center justify-between p-3 bg-gray-50 rounded">
                  <span className="text-gray-700">{type}</span>
                  <button className="text-sm text-gray-500 hover:text-gray-700">설정</button>
                </div>
              ))}
            </div>
          </div>

          {/* SLA 규칙 */}
          <div className="border-t pt-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">SLA 규칙</h2>
            <div className="space-y-3">
              {[
                { urgency: 'CRITICAL', response: '30분', resolve: '4시간' },
                { urgency: 'HIGH', response: '2시간', resolve: '1일' },
                { urgency: 'NORMAL', response: '4시간', resolve: '3일' },
                { urgency: 'LOW', response: '1일', resolve: '5일' },
              ].map(rule => (
                <div key={rule.urgency} className="flex items-center justify-between p-3 bg-gray-50 rounded">
                  <span className="font-medium text-gray-900">{rule.urgency}</span>
                  <div className="text-sm text-gray-600">
                    응답: {rule.response} / 해결: {rule.resolve}
                  </div>
                </div>
              ))}
            </div>
          </div>

          <div className="border-t pt-6">
            <button className="px-6 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors">
              설정 저장
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
