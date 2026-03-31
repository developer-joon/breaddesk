# BreadDesk 데이터 모델

## 개요
BreadDesk는 PostgreSQL + pgvector를 사용하며, 고객 문의 → AI 자동답변 → 에스컬레이션 → 업무 관리의 전체 흐름을 지원합니다.

## 엔티티 관계도 (텍스트)

```
[Member] ─┬─ 1:N ─> [Task] (assignee)
          ├─ 1:N ─> [Notification]
          ├─ 1:N ─> [PersonalNote]
          ├─ 1:N ─> [TaskWatcher]
          ├─ 1:N ─> [TaskComment]
          ├─ 1:N ─> [TaskTransfer] (from/to)
          ├─ 1:N ─> [Attachment] (uploaded_by)
          └─ 1:N ─> [ReplyTemplate] (created_by)

[Inquiry] ─┬─ 1:N ─> [InquiryMessage]
           ├─ 1:1 ─> [Task] (optional, 에스컬레이션 시)
           └─ 1:N ─> [Attachment]

[Task] ─┬─ 1:N ─> [TaskChecklist]
        ├─ 1:N ─> [TaskTag]
        ├─ 1:N ─> [TaskComment]
        ├─ 1:N ─> [TaskLog]
        ├─ 1:N ─> [TaskHold]
        ├─ 1:N ─> [TaskTransfer]
        ├─ 1:N ─> [TaskWatcher]
        ├─ 1:N ─> [TaskRelation] (source/target)
        ├─ 1:1 ─> [TaskGuide] (AI 생성)
        └─ 1:N ─> [Attachment]

[KnowledgeDocument] ─> [KnowledgeConnector] (M:1)

[SlaRule] ─> [Task] (urgency 기준 적용)
```

## 엔티티 상세

### 1. Member (팀원)
**테이블**: `members`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| name | VARCHAR(100) | 이름 |
| email | VARCHAR(200) | 이메일 (UNIQUE) |
| password_hash | VARCHAR(200) | 비밀번호 해시 |
| role | VARCHAR(20) | `AGENT`, `ADMIN` |
| skills | JSONB | 스킬 정보 (JSON) |
| is_active | BOOLEAN | 활성 여부 |
| created_at | TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | 수정 시각 |

**비즈니스 규칙**:
- 이메일은 고유해야 함
- role은 AGENT(일반 상담원) 또는 ADMIN(관리자)
- skills는 JSON 형식으로 저장 (예: `{"languages": ["한국어", "영어"], "expertise": ["결제", "환불"]}`)

---

### 2. Inquiry (문의)
**테이블**: `inquiries`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| channel | VARCHAR(50) | 채널 (slack, teams, email 등) |
| channel_meta | JSONB | 채널별 메타데이터 |
| sender_name | VARCHAR(100) | 발신자 이름 |
| sender_email | VARCHAR(200) | 발신자 이메일 |
| message | TEXT | 문의 내용 |
| ai_response | TEXT | AI 자동 답변 |
| ai_confidence | REAL | AI 신뢰도 (0.0 ~ 1.0) |
| status | VARCHAR(20) | `OPEN`, `AI_ANSWERED`, `ESCALATED`, `RESOLVED`, `CLOSED` |
| task_id | BIGINT | 연결된 Task ID (에스컬레이션 시) |
| resolved_by | VARCHAR(10) | `AI`, `HUMAN` |
| created_at | TIMESTAMP | 생성 시각 |
| resolved_at | TIMESTAMP | 해결 시각 |

**상태 흐름**:
```
OPEN → AI_ANSWERED (AI 자동답변)
     → ESCALATED (에스컬레이션, task_id 생성)
     → RESOLVED (해결)
     → CLOSED (종료)
```

---

### 3. InquiryMessage (문의 대화 이력)
**테이블**: `inquiry_messages`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| inquiry_id | BIGINT | FK → inquiries |
| role | VARCHAR(10) | `USER`, `AI`, `AGENT` |
| message | TEXT | 메시지 내용 |
| created_at | TIMESTAMP | 생성 시각 |

**비즈니스 규칙**:
- 문의 하나당 여러 대화 메시지 가능
- USER: 고객, AI: 자동답변, AGENT: 상담원

---

### 4. Task (업무)
**테이블**: `tasks`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| title | VARCHAR(500) | 제목 |
| description | TEXT | 설명 |
| type | VARCHAR(50) | 업무 타입 (예: GENERAL, BUG, FEATURE) |
| urgency | VARCHAR(20) | `LOW`, `NORMAL`, `HIGH`, `CRITICAL` |
| status | VARCHAR(20) | `WAITING`, `IN_PROGRESS`, `PENDING`, `REVIEW`, `DONE` |
| requester_name | VARCHAR(100) | 요청자 이름 |
| requester_email | VARCHAR(200) | 요청자 이메일 |
| assignee_id | BIGINT | FK → members |
| inquiry_id | BIGINT | FK → inquiries (에스컬레이션 시) |
| ai_summary | TEXT | AI 요약 |
| due_date | DATE | 마감일 |
| estimated_hours | REAL | 예상 시간 |
| actual_hours | REAL | 실제 소요 시간 |
| sla_response_deadline | TIMESTAMP | SLA 응답 기한 |
| sla_resolve_deadline | TIMESTAMP | SLA 해결 기한 |
| sla_responded_at | TIMESTAMP | 응답 시각 |
| sla_response_breached | BOOLEAN | 응답 SLA 위반 여부 |
| sla_resolve_breached | BOOLEAN | 해결 SLA 위반 여부 |
| jira_issue_key | VARCHAR(50) | Jira 이슈 키 |
| jira_issue_url | VARCHAR(500) | Jira 이슈 URL |
| transfer_count | INT | 담당자 이전 횟수 |
| created_at | TIMESTAMP | 생성 시각 |
| started_at | TIMESTAMP | 시작 시각 |
| completed_at | TIMESTAMP | 완료 시각 |

**칸반 상태**:
- WAITING: 대기 중
- IN_PROGRESS: 진행 중
- PENDING: 보류 (외부 대기)
- REVIEW: 검토 중
- DONE: 완료

---

### 5. TaskChecklist (체크리스트)
**테이블**: `task_checklists`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| task_id | BIGINT | FK → tasks |
| item_text | VARCHAR(500) | 항목 내용 |
| is_done | BOOLEAN | 완료 여부 |
| sort_order | INT | 정렬 순서 |

---

### 6. TaskTag (태그)
**테이블**: `task_tags`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| task_id | BIGINT | FK → tasks |
| tag | VARCHAR(100) | 태그명 |

**인덱스**: `idx_task_tags_tag` (tag 검색 최적화)

---

### 7. TaskComment (댓글)
**테이블**: `task_comments`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| task_id | BIGINT | FK → tasks |
| author_id | BIGINT | FK → members |
| content | TEXT | 댓글 내용 |
| is_internal | BOOLEAN | 내부 전용 댓글 여부 |
| created_at | TIMESTAMP | 생성 시각 |

**비즈니스 규칙**:
- `is_internal = true`: ADMIN 전용, 고객에게 노출 안 됨

---

### 8. TaskLog (업무 이력)
**테이블**: `task_logs`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| task_id | BIGINT | FK → tasks |
| action | VARCHAR(50) | 액션 (예: STATUS_CHANGED, ASSIGNED) |
| actor_id | BIGINT | FK → members |
| details | JSONB | 상세 정보 (JSON) |
| created_at | TIMESTAMP | 생성 시각 |

**인덱스**: `idx_task_logs_task`

---

### 9. TaskHold (보류 이력)
**테이블**: `task_holds`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| task_id | BIGINT | FK → tasks |
| reason | TEXT | 보류 사유 |
| started_at | TIMESTAMP | 보류 시작 |
| ended_at | TIMESTAMP | 보류 종료 (NULL이면 진행 중) |
| sla_paused_minutes | INT | SLA 일시정지 시간 (분) |

**비즈니스 규칙**:
- 보류 중에는 SLA 타이머 일시정지 가능

---

### 10. TaskTransfer (담당자 이전 이력)
**테이블**: `task_transfers`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| task_id | BIGINT | FK → tasks |
| from_member_id | BIGINT | FK → members |
| to_member_id | BIGINT | FK → members |
| reason | TEXT | 이전 사유 |
| created_at | TIMESTAMP | 이전 시각 |

---

### 11. TaskWatcher (업무 구독)
**테이블**: `task_watchers`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| task_id | BIGINT | FK → tasks |
| member_id | BIGINT | FK → members |
| created_at | TIMESTAMP | 구독 시각 |

**제약조건**: UNIQUE(task_id, member_id)

**비즈니스 규칙**:
- 구독한 업무의 변경사항이 알림으로 전송됨

---

### 12. TaskRelation (업무 연결)
**테이블**: `task_relations`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| source_task_id | BIGINT | FK → tasks (출발) |
| target_task_id | BIGINT | FK → tasks (목적지) |
| relation_type | VARCHAR(20) | `BLOCKS`, `RELATED`, `DUPLICATE` |

**관계 타입**:
- BLOCKS: A가 B를 블로킹 (A가 완료되어야 B 진행 가능)
- RELATED: 관련 업무
- DUPLICATE: 중복 업무

---

### 13. TaskGuide (AI 가이드)
**테이블**: `task_guides`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| task_id | BIGINT | FK → tasks |
| checklist_json | JSONB | AI 제안 체크리스트 |
| related_docs_json | JSONB | 관련 문서 링크 |
| similar_tasks_json | JSONB | 유사 업무 |
| guidelines | TEXT | 가이드라인 |
| estimated_hours | REAL | AI 예상 시간 |
| generated_at | TIMESTAMP | 생성 시각 |

**비즈니스 규칙**:
- 업무 생성 또는 할당 시 AI가 자동 생성

---

### 14. SlaRule (SLA 규칙)
**테이블**: `sla_rules`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| urgency | VARCHAR(20) | UNIQUE, `LOW`, `NORMAL`, `HIGH`, `CRITICAL` |
| response_minutes | INT | 응답 기한 (분) |
| resolve_minutes | INT | 해결 기한 (분) |
| is_active | BOOLEAN | 활성 여부 |

**기본값**:
- CRITICAL: 응답 30분, 해결 240분 (4시간)
- HIGH: 응답 120분, 해결 1440분 (24시간)
- NORMAL: 응답 240분, 해결 4320분 (3일)
- LOW: 응답 1440분, 해결 7200분 (5일)

---

### 15. Notification (알림)
**테이블**: `notifications`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| member_id | BIGINT | FK → members |
| type | VARCHAR(50) | 알림 타입 (예: TASK_ASSIGNED, SLA_WARNING) |
| title | VARCHAR(300) | 제목 |
| message | TEXT | 메시지 |
| link | VARCHAR(500) | 관련 링크 |
| is_read | BOOLEAN | 읽음 여부 |
| created_at | TIMESTAMP | 생성 시각 |

**인덱스**: `idx_notifications_member` (member_id, is_read)

---

### 16. PersonalNote (개인 메모)
**테이블**: `personal_notes`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| member_id | BIGINT | FK → members |
| content | TEXT | 메모 내용 |
| created_at | TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | 수정 시각 |

---

### 17. Attachment (첨부파일)
**테이블**: `attachments`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| entity_type | VARCHAR(20) | `INQUIRY`, `TASK` |
| entity_id | BIGINT | 대상 엔티티 ID |
| filename | VARCHAR(500) | 파일명 |
| file_path | VARCHAR(1000) | 저장 경로 |
| file_size | BIGINT | 파일 크기 (bytes) |
| mime_type | VARCHAR(100) | MIME 타입 |
| uploaded_by | BIGINT | FK → members |
| created_at | TIMESTAMP | 업로드 시각 |

**인덱스**: `idx_attachments_entity` (entity_type, entity_id)

---

### 18. ReplyTemplate (답변 템플릿)
**테이블**: `reply_templates`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| title | VARCHAR(200) | 제목 |
| category | VARCHAR(100) | 카테고리 |
| content | TEXT | 템플릿 내용 |
| usage_count | INT | 사용 횟수 |
| created_by | BIGINT | FK → members |
| created_at | TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | 수정 시각 |

---

### 19. KnowledgeDocument (지식베이스 문서)
**테이블**: `knowledge_documents`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| source | VARCHAR(50) | 소스 (notion, confluence 등) |
| source_id | VARCHAR(200) | 소스 내 ID |
| title | VARCHAR(500) | 제목 |
| content | TEXT | 본문 |
| url | VARCHAR(1000) | 원본 URL |
| tags | JSONB | 태그 |
| embedding | vector(768) | 임베딩 벡터 (pgvector) |
| connector_id | BIGINT | FK → knowledge_connectors |
| chunk_index | INT | 청크 인덱스 (긴 문서 분할 시) |
| synced_at | TIMESTAMP | 마지막 동기화 시각 |
| created_at | TIMESTAMP | 생성 시각 |

**인덱스**: `idx_knowledge_source` (source, source_id)

**비즈니스 규칙**:
- pgvector를 이용한 임베딩 검색 (RAG)
- 큰 문서는 chunk_index로 분할하여 여러 레코드로 저장

---

### 20. KnowledgeConnector (지식 커넥터 설정)
**테이블**: `knowledge_connectors`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| name | VARCHAR(200) | 커넥터 이름 |
| source_type | VARCHAR(50) | 소스 타입 (notion, confluence) |
| config | JSONB | 설정 (API 키, URL 등) |
| sync_interval_min | INT | 동기화 주기 (분) |
| last_synced_at | TIMESTAMP | 마지막 동기화 시각 |
| is_active | BOOLEAN | 활성 여부 |
| created_at | TIMESTAMP | 생성 시각 |

---

### 21. ChannelConfig (채널 설정)
**테이블**: `channel_configs`

| 필드 | 타입 | 설명 |
|------|------|------|
| id | BIGSERIAL | PK |
| channel_type | VARCHAR(50) | UNIQUE, 채널 타입 (slack, teams, email) |
| webhook_url | VARCHAR(500) | 웹훅 URL |
| auth_token | VARCHAR(500) | 인증 토큰 |
| is_active | BOOLEAN | 활성 여부 |
| config | JSONB | 추가 설정 (displayName, icon 등) |
| created_at | TIMESTAMP | 생성 시각 |
| updated_at | TIMESTAMP | 수정 시각 |

**인덱스**:
- `idx_channel_configs_type` (channel_type)
- `idx_channel_configs_active` (is_active)

**기본 채널**:
- slack, teams, email (기본 비활성)

---

## 마이그레이션 이력

### V1__init_schema.sql
- 모든 기본 테이블 생성
- pgvector 확장 활성화
- 기본 SLA 규칙 삽입

### V2__knowledge_rag_enhancements.sql
- knowledge_documents에 connector_id, chunk_index 추가
- knowledge_connectors에 name 추가

### V3__channel_configs.sql
- channel_configs 테이블 생성
- slack, teams, email 기본 채널 삽입

---

## 주요 비즈니스 규칙 요약

1. **문의 → 업무 에스컬레이션**: Inquiry.status = ESCALATED 시 Task 생성, Inquiry.task_id 연결
2. **SLA 계산**: Task 생성 시 urgency에 따라 sla_response_deadline, sla_resolve_deadline 자동 설정
3. **보류 중 SLA 일시정지**: TaskHold.ended_at = NULL인 동안 SLA 타이머 정지 가능
4. **업무 연결**: BLOCKS 관계 시 source_task가 완료되어야 target_task 진행 가능
5. **AI 자동답변**: Inquiry 생성 시 RAG 기반 유사 문의 검색 → ai_response 생성
6. **담당자 추천**: Task 생성/할당 시 AI가 member.skills와 유사 업무 기반으로 추천
