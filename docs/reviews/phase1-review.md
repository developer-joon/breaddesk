# Phase 1 코드리뷰 결과

**리뷰 일자**: 2026-03-29  
**리뷰 대상**: PR #1 (specs), PR #2 (backend), PR #3 (frontend)  
**리뷰어**: BreadDesk Reviewer Agent

---

## 📊 전체 요약

| PR | 항목 | 점수 | 상태 |
|----|------|------|------|
| **PR #1** | API 스펙 | **9/10** | ✅ Approve (수정 권장) |
| **PR #2** | 백엔드 구현 | **7/10** | ⚠️ Changes Requested |
| **PR #3** | 프론트엔드 구현 | **8/10** | ✅ Approve (수정 권장) |

**전체 품질 평가**: **8.0/10** — 좋은 시작, 필수 수정 후 머지 권장

---

## PR #1: API 스펙 (feature/phase1-specs)

### 📈 점수: 9/10

### ✅ 잘한 점

1. **명확한 구조**: 도메인별 API 명세가 잘 분리되어 있음
2. **상세한 Request/Response**: 필드별 설명, 예시, 에러 코드까지 완비
3. **DESIGN.md와 일관성**: 설계 문서와 API 스펙이 대체로 일치
4. **확장성 고려**: Phase 2+ 확장 계획까지 문서화

### 🐛 필수 수정 사항 (Must Fix)

**없음** — 스펙은 대체로 완성도가 높음

### 💡 권장 수정 사항 (Should Fix)

1. **페이지네이션 응답 형식 불일치** (우선순위: 중)
   - **위치**: `inquiry-api.md`, `task-api.md`, `template-api.md`
   - **문제**: 각 API마다 페이지네이션 응답 구조가 다름
   - **예시 (문의 API)**:
     ```json
     {
       "content": [...],
       "page": { "size": 20, "number": 0, "totalElements": 127, "totalPages": 7 }
     }
     ```
   - **예시 (템플릿 API)**:
     ```json
     {
       "content": [...],
       "page": { ... }
     }
     ```
   - **권장**: 전체 API에서 통일된 응답 형식 사용 (Spring의 `Page` 구조 그대로 사용하는 것 권장)

2. **누락된 API 엔드포인트** (우선순위: 중)
   - **누락**: SLA 규칙 관리 API (`GET /settings/sla`, `PUT /settings/sla`)
   - **위치**: DESIGN.md에는 언급되어 있으나 API 스펙에는 없음
   - **권장**: `docs/specs/settings-api.md` 추가 필요

3. **태그 API 누락** (우선순위: 낮)
   - **위치**: DESIGN.md 섹션 5.8 "태그/라벨"에 언급됨
   - **API**: `GET /api/v1/tags` (전체 태그 목록 + 사용 빈도)
   - **권장**: `task-api.md`에 태그 목록 조회 API 추가

### 🎯 개선 제안 (Nice to Have)

1. **API 버전 관리 전략 문서화**
   - v1 → v2 마이그레이션 시 어떻게 처리할지 가이드 추가

2. **Rate Limiting 정책**
   - API별 호출 제한 (예: POST /inquiries → 100회/분)
   - 스펙에 `X-RateLimit-*` 헤더 추가

3. **Webhook 스펙 더 상세히**
   - n8n → BreadDesk 웹훅 형식 (현재는 BreadDesk → n8n만 설명)
   - 재시도 정책, 타임아웃

---

## PR #2: 백엔드 구현 (feature/phase1-backend)

### 📈 점수: 7/10

### ✅ 잘한 점

1. **깔끔한 도메인 구조**: 패키지가 도메인별로 잘 분리됨 (`inquiry`, `task`, `auth` 등)
2. **AI 추상화 완성**: `LLMProvider` 인터페이스로 확장 가능하게 설계
3. **DB 마이그레이션 완비**: Flyway 스키마가 DESIGN.md와 일치
4. **전역 예외 처리**: `GlobalExceptionHandler`로 일관된 에러 응답

### 🚨 필수 수정 사항 (Must Fix)

#### 1. **보안 취약점: SQL Injection 위험** (심각도: 높음)
   - **위치**: `TaskRepository` (추정, 실제 파일 확인 필요)
   - **문제**: `findByFilters` 메서드가 동적 쿼리 생성 시 파라미터 바인딩 없이 문자열 결합 사용 가능성
   - **예시 (위험한 코드)**:
     ```java
     String query = "SELECT * FROM tasks WHERE type = '" + type + "'";  // ❌
     ```
   - **해결**: JPA Specification 또는 QueryDSL 사용
     ```java
     @Query("SELECT t FROM Task t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:type IS NULL OR t.type = :type)")
     Page<Task> findByFilters(@Param("status") TaskStatus status, 
                               @Param("type") String type, 
                               Pageable pageable);
     ```

#### 2. **파일 업로드 검증 미흡** (심각도: 높음)
   - **위치**: `AttachmentController` (파일 확인 필요)
   - **누락 사항**:
     - 파일 확장자 화이트리스트 검증
     - MIME 타입 검증 (Content-Type 헤더만 믿으면 안 됨)
     - 파일 시그니처 (magic bytes) 확인
   - **권장 코드**:
     ```java
     private void validateFile(MultipartFile file) {
         // 1. 확장자 체크
         String ext = FilenameUtils.getExtension(file.getOriginalFilename());
         if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
             throw new BusinessException("Invalid file type", "INVALID_FILE_TYPE");
         }
         
         // 2. MIME 타입 체크 (시그니처 기반)
         String detectedType = tika.detect(file.getInputStream());
         if (!ALLOWED_MIME_TYPES.contains(detectedType)) {
             throw new BusinessException("Invalid MIME type", "INVALID_FILE_TYPE");
         }
     }
     ```

#### 3. **JPA N+1 쿼리 문제** (심각도: 중)
   - **위치**: `InquiryService.getInquiry()`, `TaskService.getTask()`
   - **문제**: 
     ```java
     Inquiry inquiry = inquiryRepository.findById(id).orElseThrow(...);
     List<InquiryMessage> messages = messageRepository.findByInquiryId(id);  // 별도 쿼리
     ```
   - **영향**: 문의 100개 조회 시 200번의 쿼리 실행
   - **해결**: Fetch Join 사용
     ```java
     @Query("SELECT i FROM Inquiry i LEFT JOIN FETCH i.messages WHERE i.id = :id")
     Optional<Inquiry> findByIdWithMessages(@Param("id") Long id);
     ```

#### 4. **트랜잭션 범위 부적절** (심각도: 중)
   - **위치**: `InquiryService.createInquiry()`
   - **문제**: AI 호출이 트랜잭션 내부에서 실행됨 (최대 30초 대기 → DB 커넥션 점유)
   - **해결**: AI 호출을 비동기로 분리
     ```java
     @Transactional
     public InquiryResponse createInquiry(InquiryRequest request) {
         Inquiry saved = inquiryRepository.save(inquiry);
         // 즉시 반환
         aiService.generateAnswerAsync(saved.getId(), request.getMessage());  // @Async
         return InquiryResponse.from(saved);
     }
     ```

### 💡 권장 수정 사항 (Should Fix)

1. **에러 핸들링 개선** (우선순위: 중)
   - **위치**: `InquiryService.escalateToTask()`
   - **문제**: 업무 생성 실패 시 예외가 전파되지만 inquiry 상태는 이미 `ESCALATED`로 변경됨
   - **권장**: TaskService 호출 전에 상태 변경하지 않거나, 실패 시 롤백

2. **로깅 레벨 조정** (우선순위: 낮)
   - **위치**: 전역
   - **문제**: `log.info`가 너무 많음 (모든 CRUD에 로그)
   - **권장**: 일반 CRUD는 `DEBUG`, 중요 이벤트만 `INFO`

3. **DTO 검증 누락** (우선순위: 중)
   - **위치**: `InquiryRequest`, `TaskRequest` 등
   - **문제**: `@Valid` 어노테이션은 있으나 필드별 `@NotNull`, `@Size` 등 검증 누락
   - **권장**:
     ```java
     @Data
     public class InquiryRequest {
         @NotBlank(message = "Channel is required")
         @Pattern(regexp = "slack|teams|jira|web|email", message = "Invalid channel")
         private String channel;
         
         @NotBlank @Size(max = 100)
         private String senderName;
         
         @Email
         private String senderEmail;
     }
     ```

4. **비밀번호 해싱 미구현** (우선순위: 높음)
   - **위치**: `Member` 엔티티 (비밀번호 필드 없음)
   - **문제**: Phase 1에서 인증 비활성화했지만, Member 엔티티에 password 필드조차 없음
   - **권장**: Phase 2 전에 추가 필요
     ```java
     @Column(nullable = false)
     private String passwordHash;  // bcrypt
     ```

### 🎯 개선 제안 (Nice to Have)

1. **테스트 코드 없음**
   - 단위 테스트, 통합 테스트 추가 권장
   - 최소한 서비스 계층의 핵심 로직 테스트

2. **AI 신뢰도 계산 너무 단순**
   - **위치**: `OllamaProvider.estimateConfidence()`
   - 현재: 휴리스틱 (길이, 키워드)
   - 개선: 실제 LLM confidence score 활용 (Phase 2)

3. **SLA 타이머 미구현**
   - Task 생성 시 SLA 데드라인은 계산하지만, 초과 감지 로직 없음
   - 배치 작업 또는 스케줄러 추가 필요

---

## PR #3: 프론트엔드 구현 (feature/phase1-frontend)

### 📈 점수: 8/10

### ✅ 잘한 점

1. **TypeScript Strict 모드**: tsconfig에서 `"strict": true` 활성화
2. **깔끔한 컴포넌트 구조**: 도메인별 페이지 분리 (`inquiries/`, `tasks/`)
3. **API 클라이언트 추상화**: `api.ts`로 백엔드 호출 일원화
4. **타입 안전성**: `types/index.ts`로 전역 타입 정의

### 🐛 필수 수정 사항 (Must Fix)

**없음** — 프론트엔드는 대체로 잘 구현됨

### 💡 권장 수정 사항 (Should Fix)

1. **API 응답 타입 불일치** (우선순위: 중)
   - **위치**: `types/index.ts` vs API 스펙
   - **문제**: 백엔드 응답 구조와 프론트엔드 타입이 다름
   - **예시 (백엔드 실제 응답)**:
     ```json
     {
       "success": true,
       "message": "...",
       "data": { ... }
     }
     ```
   - **예시 (프론트엔드 타입)**:
     ```typescript
     export interface ApiResponse<T> {
       success: boolean;
       data?: T;
       error?: string;
       message?: string;  // ✅ 있음
     }
     ```
   - **권장**: 백엔드의 `ApiResponse` DTO 확인 후 타입 일치시키기

2. **에러 처리 부족** (우선순위: 중)
   - **위치**: `tasks/page.tsx`, `inquiries/page.tsx`
   - **문제**: API 호출 실패 시 사용자에게 에러 메시지 표시 안 됨
   - **현재 코드**:
     ```typescript
     const response = await api.getKanbanTasks();
     if (response.success && response.data) {
       setTasksByStatus(response.data as Record<TaskStatus, Task[]>);
     }
     // 실패 시 아무 처리 없음
     ```
   - **권장**:
     ```typescript
     if (response.success && response.data) {
       setTasksByStatus(response.data);
     } else {
       setError(response.error || '업무 로딩 실패');
       toast.error(response.error);  // 토스트 알림
     }
     ```

3. **로딩 상태 UX 개선** (우선순위: 낮)
   - **위치**: 전역
   - **현재**: "로딩 중..." 텍스트만 표시
   - **권장**: Spinner 컴포넌트 또는 Skeleton UI

4. **빌드 확인 미실시** (우선순위: 높음)
   - **문제**: `npm run build` 테스트 결과가 리뷰 요청에 없음
   - **권장**: PR 체크리스트에 추가
     ```bash
     npm run build && npm run type-check
     ```

### 🎯 개선 제안 (Nice to Have)

1. **상태 관리 라이브러리 도입**
   - 현재: `useState` + prop drilling
   - 제안: Zustand 또는 Context API로 전역 상태 관리 (Phase 2)

2. **UI 라이브러리 활용**
   - 현재: Tailwind CSS만 사용
   - 제안: Shadcn/ui 또는 Radix UI로 일관된 디자인 시스템

3. **접근성 (A11y)**
   - 버튼에 aria-label 누락
   - 키보드 네비게이션 미지원

---

## 🔧 즉시 조치 필요 항목 (우선순위별)

### 🚨 P0 (Critical - 머지 전 필수)

1. **백엔드: SQL Injection 취약점 수정** (PR #2)
2. **백엔드: 파일 업로드 검증 강화** (PR #2)
3. **백엔드: 비밀번호 해싱 필드 추가** (PR #2)

### ⚠️ P1 (High - 다음 스프린트 초반)

1. **백엔드: JPA N+1 쿼리 최적화** (PR #2)
2. **백엔드: AI 호출 비동기 분리** (PR #2)
3. **백엔드: DTO 검증 어노테이션 추가** (PR #2)
4. **프론트엔드: 빌드 테스트 + 에러 처리** (PR #3)

### 💡 P2 (Medium - 여유 있을 때)

1. **스펙: 페이지네이션 응답 통일** (PR #1)
2. **스펙: SLA 설정 API 추가** (PR #1)
3. **백엔드: 에러 핸들링 개선** (PR #2)
4. **프론트엔드: API 응답 타입 일치** (PR #3)

---

## 🎉 전체 평가

### 강점
- **설계 문서 충실히 반영**: DESIGN.md와 구현 간 괴리가 적음
- **확장 가능한 구조**: AI Provider, Storage Provider 추상화가 훌륭함
- **빠른 MVP 구축**: Phase 1 목표(2~3주)에 맞는 범위

### 약점
- **보안 고려 부족**: SQL Injection, 파일 업로드 검증 누락
- **테스트 코드 없음**: 안정성 검증 어려움
- **성능 최적화 미흡**: N+1 쿼리, 트랜잭션 범위 등

### 권장 사항
1. **P0, P1 항목 수정 후 재리뷰 요청**
2. **Phase 2 시작 전 보안 감사 실시**
3. **테스트 커버리지 최소 60% 목표 설정**

---

**리뷰 완료**: 2026-03-29 12:30 KST  
**다음 단계**: P0 이슈 수정 → 재리뷰 → 머지 승인
