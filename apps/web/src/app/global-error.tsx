'use client';

export default function GlobalError({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  return (
    <html lang="ko">
      <body>
        <div style={{ padding: '2rem', fontFamily: 'sans-serif' }}>
          <h2>오류가 발생했습니다</h2>
          <p style={{ color: 'red' }}>{error.message}</p>
          <pre style={{ background: '#f5f5f5', padding: '1rem', overflow: 'auto', fontSize: '12px' }}>
            {error.stack}
          </pre>
          <button onClick={() => reset()} style={{ padding: '0.5rem 1rem', marginTop: '1rem' }}>
            다시 시도
          </button>
        </div>
      </body>
    </html>
  );
}
