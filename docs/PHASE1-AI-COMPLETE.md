# Phase 1 — AI 핵심 기능 구현 완료 ✅

**PR**: https://github.com/developer-joon/breaddesk/pull/9  
**브랜치**: `feature/phase1-ai`  
**완료일**: 2026-04-01

---

## 구현 완료 항목

### 1. RAG 기반 AI 자동응답 ⭐ (최우선)
- ✅ 문의 접수 시 벡터 검색으로 관련 문서 3~5개 조회
- ✅ LLM에 질문 + 관련 문서 전달 → 답변 생성
- ✅ 신뢰도 판단 (높음: 자동 답변, 중간: 답변+에스컬레이션, 낮음: 에스컬레이션)
- ✅ AI 응답 + 참고 문서 출처 표시
- ✅ 프론트: 문의 상세 페이지에서 AI 답변 표시 (신뢰도 포함)

### 2. AI 응답 추천 (Copilot) ⭐
- ✅ 상담원이 답변 작성할 때 AI가 초안 제시
- ✅ 과거 유사 문의의 답변 + 지식베이스 문서 기반
- ✅ 프론트: "🤖 AI 추천" 버튼 → 초안 표시 → 수정 후 전송

### 3. AI 응답 리라이트
- ✅ 작성한 답변을 톤 조절: 친절하게 / 공식적으로 / 간결하게
- ✅ 프론트: "✨ 리라이트" 드롭다운 버튼

### 4. AI 자동 분류/라우팅
- ✅ 문의 접수 시 카테고리 자동 분류 (DEVELOPMENT, ACCESS, INFRA 등)
- ✅ 긴급도 자동 판단 (LOW/NORMAL/HIGH/CRITICAL)
- ✅ 담당팀 자동 배정 추천 (기존 AIAssignmentService 활용)
- ✅ LLM 실패 시 키워드 기반 fallback 로직

### 5. LLM Provider 추상화 확장
- ✅ OpenAI Provider: GPT-4o + text-embedding-3-small
- ✅ Claude Provider: Claude Sonnet 4.5
- ✅ Ollama Provider: 기존 유지 (로컬 LLM)
- ✅ API 키 없을 때 graceful fallback (AI 기능 비활성화 표시)

---

## 기술 스펙

### 백엔드
- **LLM Provider**: Ollama, OpenAI, Claude 중 선택 가능
- **임베딩**: OpenAI text-embedding-3-small (또는 로컬 모델)
- **벡터 검색**: pgvector cosine similarity
- **API 키 관리**: 환경변수 (`OPENAI_API_KEY`, `ANTHROPIC_API_KEY`)
- **Fallback**: 설정 없으면 AI 기능 비활성화 안내

### 프론트엔드
- **aiService 모듈**: `apps/web/src/services/ai.ts`
- **AI 기능 UI**:
  - 🤖 AI 추천 버튼 (Copilot)
  - ✨ 리라이트 드롭다운 (친절/공식/간결)
  - AI 자동 답변 표시 (신뢰도 포함)

### API 엔드포인트
```
GET  /api/v1/ai/status                           — AI 서비스 상태 확인
POST /api/v1/ai/inquiries/{id}/suggest-reply     — AI 응답 추천 (Copilot)
POST /api/v1/ai/rewrite                          — 답변 리라이트
POST /api/v1/ai/classify/inquiry/{id}            — 문의 자동 분류
POST /api/v1/ai/classify/task/{id}               — 업무 자동 분류
POST /api/v1/ai/classify/text                    — 텍스트 분류
GET  /api/v1/ai/tasks/{id}/recommend-assignees   — AI 담당자 추천
```

---

## 빌드 확인

- ✅ 백엔드 컴파일 성공 (`./gradlew compileJava -x test`)
- ✅ 프론트엔드 빌드 성공 (`npm run build`)
- ✅ LLM Provider 인터페이스 준수
- ✅ AI 서비스 미설정 시 fallback 동작 확인

---

## 사용 방법

### 1. LLM Provider 설정

#### OpenAI 사용
```yaml
# application.yml
breaddesk:
  llm:
    provider: openai
    openai:
      model: gpt-4o
      embedding-model: text-embedding-3-small
```

환경변수:
```bash
export OPENAI_API_KEY=sk-...
```

#### Claude 사용
```yaml
breaddesk:
  llm:
    provider: claude
    claude:
      model: claude-sonnet-4-5-20250514
```

환경변수:
```bash
export ANTHROPIC_API_KEY=sk-ant-...
```

#### Ollama 사용 (로컬)
```yaml
breaddesk:
  llm:
    provider: ollama
    ollama:
      url: http://localhost:11434
      model: llama3.1:8b
```

### 2. 프론트엔드 사용

1. 문의 상세 페이지로 이동
2. 에스컬레이션된 문의 선택
3. "🤖 AI 추천" 버튼 클릭 → 초안 자동 생성
4. 필요 시 "✨ 리라이트" → 톤 조절
5. 전송

---

## 다음 단계 (Phase 2 — 업무관리 강화)

### 2-1. 문의→태스크 원클릭 전환 강화
- [ ] AI가 제목/설명/체크리스트 자동 생성
- [ ] 문의↔업무 양방향 추적 UI

### 2-2. 칸반보드
- [ ] 드래그앤드롭 상태 관리
- [ ] 담당자별/유형별/긴급도별 필터
- [ ] 칸반 + 리스트 뷰 전환

### 2-3. 팀/멀티 테넌트
- [ ] 팀(부서) CRUD
- [ ] 팀별 문의 인박스 분리
- [ ] 팀 간 이관 기능

---

## 참고

- **DESIGN.md**: 섹션 3.1 (문의 접수 & AI 자동답변), 섹션 7 (LLM 추상화)
- **PR**: https://github.com/developer-joon/breaddesk/pull/9
- **GitHub Repo**: botjoonwoo/breaddesk (private)

---

**작성**: 브래드 (AI 비서)  
**일시**: 2026-04-01 07:41 KST
