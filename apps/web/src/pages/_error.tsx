function Error({ statusCode }: { statusCode?: number }) {
  return (
    <div style={{ 
      display: 'flex', alignItems: 'center', justifyContent: 'center', 
      minHeight: '100vh', fontFamily: 'sans-serif',
      background: 'linear-gradient(135deg, #eff6ff, #e0e7ff)'
    }}>
      <div style={{ textAlign: 'center' }}>
        <div style={{ fontSize: '5rem', marginBottom: '1rem' }}>🍞</div>
        <h1 style={{ fontSize: '2rem', color: '#111827', margin: '0 0 0.5rem' }}>
          {statusCode || '오류'}
        </h1>
        <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
          {statusCode === 404 ? '페이지를 찾을 수 없습니다' : '오류가 발생했습니다'}
        </p>
        <a href="/" style={{
          padding: '0.5rem 1.5rem', backgroundColor: '#2563eb', color: 'white',
          borderRadius: '0.5rem', textDecoration: 'none'
        }}>
          홈으로 돌아가기
        </a>
      </div>
    </div>
  );
}

Error.getInitialProps = ({ res, err }: { res?: { statusCode: number }; err?: { statusCode: number } }) => {
  const statusCode = res ? res.statusCode : err ? err.statusCode : 404;
  return { statusCode };
};

export default Error;
