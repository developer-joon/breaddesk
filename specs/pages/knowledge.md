# 지식베이스 페이지

## 경로
`/knowledge`

## 인증
필요 (AGENT, ADMIN)

## 주요 컴포넌트
- **검색 바**: RAG 기반 벡터 검색
- **문서 목록**: 제목, 소스, 마지막 동기화 시각
- **커넥터 관리 (ADMIN 전용)**: Notion, Confluence 등 설정
- **문서 상세 모달**: 제목, 본문, 원본 링크

## 데이터 소스
- `GET /api/v1/knowledge/documents?keyword=...` - 문서 목록
- `GET /api/v1/knowledge/documents/{id}` - 문서 상세
- `POST /api/v1/knowledge/search` - 벡터 검색
- `GET /api/v1/knowledge/connectors` - 커넥터 목록 (ADMIN)
- `POST /api/v1/knowledge/connectors` - 커넥터 생성 (ADMIN)
- `PUT /api/v1/knowledge/connectors/{id}` - 커넥터 수정 (ADMIN)

## 사용자 인터랙션

### 문서 검색
- 검색 바에 질문 입력 (예: "결제 오류 어떻게 해결하나요?")
- debounce 후 `POST /api/v1/knowledge/search` 호출
- 유사도 순으로 결과 표시
- 문서 클릭 → 상세 모달

### 문서 목록
- 키워드 검색 (제목/내용)
- 소스별 필터 (Notion, Confluence 등)
- 페이지네이션

### 커넥터 관리 (ADMIN 전용)
- "+ 커넥터 추가" 버튼 클릭
- 소스 타입 선택 (Notion, Confluence)
- API 키, URL 등 설정 입력
- "저장" 버튼 → `POST /api/v1/knowledge/connectors`
- 동기화 주기 설정 (분 단위)
- 수동 동기화 버튼 (향후 구현)

## 상태 관리
- React Query: 문서 목록, 검색 결과 캐싱
- Zustand: 필터 상태

## 구현 참고
- `apps/web/src/app/knowledge/page.tsx`
- `apps/web/src/services/knowledge.ts`
