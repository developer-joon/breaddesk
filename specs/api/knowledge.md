# 지식베이스 API

## 개요
지식베이스 문서 관리 및 RAG(Retrieval-Augmented Generation) 기반 벡터 검색을 지원합니다. Notion, Confluence 등 외부 소스와 동기화할 수 있습니다.

## 엔드포인트

### GET /api/v1/knowledge/documents
**설명**: 지식베이스 문서 목록을 조회합니다. 키워드 검색을 지원합니다.

**인증**: 필요 (AGENT, ADMIN)

**Query Parameters**:
- `keyword`: 제목/내용 검색 (선택)
- `page`: 페이지 번호 (0부터 시작)
- `size`: 페이지 크기 (기본값: 20)

**Response**:
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "source": "notion",
        "sourceId": "abc123",
        "title": "결제 오류 처리 가이드",
        "content": "고객이 결제 오류를 겪는 경우...",
        "url": "https://notion.so/abc123",
        "tags": ["결제", "가이드"],
        "syncedAt": "2026-03-31T10:00:00",
        "createdAt": "2026-03-01T12:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

---

### GET /api/v1/knowledge/documents/{id}
**설명**: 특정 문서의 상세 정보를 조회합니다.

**인증**: 필요 (AGENT, ADMIN)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "source": "notion",
    "sourceId": "abc123",
    "title": "결제 오류 처리 가이드",
    "content": "고객이 결제 오류를 겪는 경우...",
    "url": "https://notion.so/abc123",
    "tags": ["결제", "가이드"],
    "connectorId": 1,
    "chunkIndex": 0,
    "syncedAt": "2026-03-31T10:00:00",
    "createdAt": "2026-03-01T12:00:00"
  }
}
```

**에러 코드**:
- `404 Not Found`: 문서를 찾을 수 없음

---

### POST /api/v1/knowledge/search
**설명**: RAG 기반 벡터 검색을 수행합니다. 임베딩 유사도 기반으로 관련 문서를 찾습니다.

**인증**: 필요 (AGENT, ADMIN)

**Request Body**:
```json
{
  "query": "결제가 안 될 때 어떻게 해야 하나요?",
  "limit": 5
}
```

**Response**:
```json
{
  "success": true,
  "data": [
    [
      1,
      "결제 오류 처리 가이드",
      "고객이 결제 오류를 겪는 경우...",
      "https://notion.so/abc123",
      0.92
    ],
    [
      5,
      "PG사 연동 오류 대응",
      "PG사 API 오류 시...",
      "https://notion.so/def456",
      0.87
    ]
  ]
}
```

**응답 형식**:
```
[id, title, content_snippet, url, similarity_score]
```

**비즈니스 규칙**:
- 임베딩 벡터는 768차원 (OpenAI text-embedding-ada-002 등)
- 코사인 유사도 기준 상위 N개 반환
- 최소 유사도 threshold: 0.7

---

### GET /api/v1/knowledge/connectors
**설명**: 커넥터 목록을 조회합니다.

**인증**: 필요 (ADMIN)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "회사 Notion",
      "sourceType": "notion",
      "config": {
        "apiKey": "secret_***",
        "databaseId": "abc123"
      },
      "syncIntervalMin": 60,
      "lastSyncedAt": "2026-03-31T10:00:00",
      "isActive": true,
      "createdAt": "2026-03-01T12:00:00"
    }
  ]
}
```

---

### POST /api/v1/knowledge/connectors
**설명**: 새로운 커넥터를 생성합니다.

**인증**: 필요 (ADMIN)

**Request Body**:
```json
{
  "name": "회사 Confluence",
  "sourceType": "confluence",
  "config": {
    "url": "https://company.atlassian.net",
    "username": "user@company.com",
    "apiToken": "ATATT..."
  },
  "syncIntervalMin": 120,
  "isActive": true
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": 2,
    "name": "회사 Confluence",
    "sourceType": "confluence",
    "config": {...},
    "syncIntervalMin": 120,
    "isActive": true,
    "createdAt": "2026-03-31T12:00:00"
  }
}
```

**지원되는 sourceType**:
- `notion`: Notion API
- `confluence`: Confluence Cloud API
- (향후 추가: `google-drive`, `sharepoint` 등)

**에러 코드**:
- `400 Bad Request`: 필수 필드 누락 또는 유효하지 않은 config

---

### PUT /api/v1/knowledge/connectors/{id}
**설명**: 커넥터를 수정합니다.

**인증**: 필요 (ADMIN)

**Request Body**: POST와 동일

**Response**: 수정된 커넥터 반환

**에러 코드**:
- `404 Not Found`: 커넥터를 찾을 수 없음

---

## 커넥터 설정 예시

### Notion
```json
{
  "sourceType": "notion",
  "config": {
    "apiKey": "secret_***",
    "databaseId": "abc123",
    "filterProperty": "Status",
    "filterValue": "Published"
  }
}
```

### Confluence
```json
{
  "sourceType": "confluence",
  "config": {
    "url": "https://company.atlassian.net",
    "username": "user@company.com",
    "apiToken": "ATATT...",
    "spaceKey": "DOCS"
  }
}
```

---

## 동기화 프로세스

1. **스케줄러**: `syncIntervalMin` 주기로 자동 동기화
2. **문서 수집**: 외부 소스에서 문서 목록 가져오기
3. **청킹**: 긴 문서는 chunk_index로 분할하여 저장
4. **임베딩 생성**: 각 청크에 대해 임베딩 벡터 생성 (EmbeddingService)
5. **pgvector 저장**: knowledge_documents 테이블에 저장

---

## 비즈니스 규칙

1. **중복 방지**: (source, source_id) 조합이 고유해야 함
2. **청킹 전략**: 최대 2000자 단위로 분할, 200자 오버랩
3. **임베딩 모델**: OpenAI text-embedding-ada-002 (768차원)
4. **검색 threshold**: 유사도 < 0.7인 결과는 필터링
5. **동기화 오류 처리**: 실패 시 lastSyncedAt 업데이트 안 함, 에러 로그 기록

---

## 프론트엔드 구현 참고

- `apps/web/src/services/knowledge.ts`: API 호출 함수
- `apps/web/src/app/knowledge/page.tsx`: 지식베이스 페이지
- 검색 UI: 입력 시 debounce 적용, 실시간 검색 결과 표시
- 커넥터 설정: ADMIN 전용 UI

---

## 관련 엔티티

- [KnowledgeDocument](../data-model/ENTITIES.md#19-knowledgedocument-지식베이스-문서)
- [KnowledgeConnector](../data-model/ENTITIES.md#20-knowledgeconnector-지식-커넥터-설정)
