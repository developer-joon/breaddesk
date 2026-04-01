'use client';

import React, { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';
import { csatService } from '@/services/csat';
import type { CsatSurvey } from '@/services/csat';
import toast from 'react-hot-toast';

export default function SurveyPage() {
  const params = useParams();
  const token = params.token as string;
  
  const [survey, setSurvey] = useState<CsatSurvey | null>(null);
  const [loading, setLoading] = useState(true);
  const [rating, setRating] = useState(0);
  const [feedback, setFeedback] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    loadSurvey();
  }, [token]);

  const loadSurvey = async () => {
    try {
      setLoading(true);
      const data = await csatService.getSurvey(token);
      setSurvey(data);
      if (data.submittedAt) {
        setSubmitted(true);
        setRating(data.rating || 0);
        setFeedback(data.feedback || '');
      }
    } catch (error) {
      console.error('Failed to load survey:', error);
      toast.error('설문을 불러올 수 없습니다');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async () => {
    if (rating === 0) {
      toast.error('만족도를 선택해주세요');
      return;
    }

    try {
      setSubmitting(true);
      await csatService.submitSurvey(token, { rating, feedback });
      setSubmitted(true);
      toast.success('설문에 참여해주셔서 감사합니다!');
    } catch (error) {
      console.error('Failed to submit survey:', error);
      toast.error('설문 제출에 실패했습니다');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (!survey) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md text-center">
          <div className="text-6xl mb-4">❌</div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">설문을 찾을 수 없습니다</h1>
          <p className="text-gray-600">유효하지 않거나 만료된 링크입니다.</p>
        </div>
      </div>
    );
  }

  if (submitted) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="bg-white rounded-lg shadow-lg p-8 max-w-md text-center">
          <div className="text-6xl mb-4">🎉</div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">감사합니다!</h1>
          <p className="text-gray-600 mb-4">
            고객님의 소중한 의견이 전달되었습니다.
          </p>
          <div className="bg-gray-50 rounded-lg p-4">
            <div className="flex justify-center gap-1 mb-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <span
                  key={star}
                  className={`text-3xl ${
                    star <= rating ? 'text-yellow-500' : 'text-gray-300'
                  }`}
                >
                  ⭐
                </span>
              ))}
            </div>
            {feedback && (
              <p className="text-sm text-gray-700 mt-3 italic">"{feedback}"</p>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center py-8 px-4">
      <div className="bg-white rounded-lg shadow-lg p-8 max-w-md w-full">
        <div className="text-center mb-8">
          <div className="text-5xl mb-4">📊</div>
          <h1 className="text-2xl font-bold text-gray-900 mb-2">고객 만족도 조사</h1>
          <p className="text-gray-600">
            최근 문의 응대에 대한 만족도를 평가해주세요
          </p>
        </div>

        <div className="space-y-6">
          {/* Star Rating */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-3 text-center">
              서비스 만족도 <span className="text-red-500">*</span>
            </label>
            <div className="flex justify-center gap-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  key={star}
                  onClick={() => setRating(star)}
                  className={`text-5xl transition-transform hover:scale-110 ${
                    star <= rating ? 'text-yellow-500' : 'text-gray-300 hover:text-yellow-400'
                  }`}
                  title={`${star}점`}
                >
                  ⭐
                </button>
              ))}
            </div>
            {rating > 0 && (
              <p className="text-center mt-2 text-sm text-gray-600">
                {rating === 5 && '매우 만족'}
                {rating === 4 && '만족'}
                {rating === 3 && '보통'}
                {rating === 2 && '불만족'}
                {rating === 1 && '매우 불만족'}
              </p>
            )}
          </div>

          {/* Feedback Text */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              추가 의견 (선택)
            </label>
            <textarea
              value={feedback}
              onChange={(e) => setFeedback(e.target.value)}
              placeholder="개선할 점이나 좋았던 점을 자유롭게 작성해주세요..."
              rows={5}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              maxLength={500}
            />
            <p className="text-xs text-gray-500 mt-1 text-right">
              {feedback.length} / 500
            </p>
          </div>

          {/* Submit Button */}
          <button
            onClick={handleSubmit}
            disabled={submitting || rating === 0}
            className="w-full px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
          >
            {submitting ? '제출 중...' : '제출하기'}
          </button>
        </div>

        <div className="mt-6 text-center text-xs text-gray-500">
          <p>귀하의 의견은 서비스 개선에 큰 도움이 됩니다.</p>
        </div>
      </div>
    </div>
  );
}
