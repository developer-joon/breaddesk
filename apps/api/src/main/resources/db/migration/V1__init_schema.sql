-- BreadDesk 초기 스키마 (PostgreSQL + pgvector)
-- Version: 1.0

-- pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 팀원
CREATE TABLE members (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    password_hash VARCHAR(200),
    role VARCHAR(20) NOT NULL DEFAULT 'AGENT' CHECK (role IN ('AGENT', 'ADMIN')),
    skills JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 문의
CREATE TABLE inquiries (
    id BIGSERIAL PRIMARY KEY,
    channel VARCHAR(50) NOT NULL,
    channel_meta JSONB,
    sender_name VARCHAR(100) NOT NULL,
    sender_email VARCHAR(200),
    message TEXT NOT NULL,
    ai_response TEXT,
    ai_confidence REAL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'AI_ANSWERED', 'ESCALATED', 'RESOLVED', 'CLOSED')),
    task_id BIGINT,
    resolved_by VARCHAR(10) CHECK (resolved_by IN ('AI', 'HUMAN')),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- 문의 대화 이력
CREATE TABLE inquiry_messages (
    id BIGSERIAL PRIMARY KEY,
    inquiry_id BIGINT NOT NULL REFERENCES inquiries(id) ON DELETE CASCADE,
    role VARCHAR(10) NOT NULL CHECK (role IN ('USER', 'AI', 'AGENT')),
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- SLA 규칙
CREATE TABLE sla_rules (
    id BIGSERIAL PRIMARY KEY,
    urgency VARCHAR(20) NOT NULL UNIQUE CHECK (urgency IN ('LOW', 'NORMAL', 'HIGH', 'CRITICAL')),
    response_minutes INT NOT NULL,
    resolve_minutes INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 기본 SLA 규칙
INSERT INTO sla_rules (urgency, response_minutes, resolve_minutes) VALUES
    ('CRITICAL', 30, 240),
    ('HIGH', 120, 1440),
    ('NORMAL', 240, 4320),
    ('LOW', 1440, 7200);

-- 업무
CREATE TABLE tasks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    urgency VARCHAR(20) NOT NULL DEFAULT 'NORMAL' CHECK (urgency IN ('LOW', 'NORMAL', 'HIGH', 'CRITICAL')),
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING' CHECK (status IN ('WAITING', 'IN_PROGRESS', 'REVIEW', 'DONE')),
    requester_name VARCHAR(100),
    requester_email VARCHAR(200),
    assignee_id BIGINT REFERENCES members(id) ON DELETE SET NULL,
    inquiry_id BIGINT REFERENCES inquiries(id) ON DELETE SET NULL,
    ai_summary TEXT,
    due_date DATE,
    estimated_hours REAL,
    actual_hours REAL,
    sla_response_deadline TIMESTAMP,
    sla_resolve_deadline TIMESTAMP,
    sla_responded_at TIMESTAMP,
    sla_response_breached BOOLEAN NOT NULL DEFAULT FALSE,
    sla_resolve_breached BOOLEAN NOT NULL DEFAULT FALSE,
    jira_issue_key VARCHAR(50),
    jira_issue_url VARCHAR(500),
    transfer_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- 업무 전달 이력
CREATE TABLE task_transfers (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    from_member_id BIGINT REFERENCES members(id) ON DELETE SET NULL,
    to_member_id BIGINT REFERENCES members(id) ON DELETE SET NULL,
    reason TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 업무 AI 가이드 (할당 시 자동 생성)
CREATE TABLE task_guides (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    checklist_json JSONB,
    related_docs_json JSONB,
    similar_tasks_json JSONB,
    guidelines TEXT,
    estimated_hours REAL,
    generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- inquiries.task_id FK
ALTER TABLE inquiries ADD CONSTRAINT fk_inquiry_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL;

-- 체크리스트
CREATE TABLE task_checklists (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    item_text VARCHAR(500) NOT NULL,
    is_done BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0
);

-- 업무 태그
CREATE TABLE task_tags (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL
);
CREATE INDEX idx_task_tags_tag ON task_tags(tag);

-- 업무 연결
CREATE TABLE task_relations (
    id BIGSERIAL PRIMARY KEY,
    source_task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    target_task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    relation_type VARCHAR(20) NOT NULL CHECK (relation_type IN ('BLOCKS', 'RELATED', 'DUPLICATE'))
);

-- 파일 첨부
CREATE TABLE attachments (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(20) NOT NULL CHECK (entity_type IN ('INQUIRY', 'TASK')),
    entity_id BIGINT NOT NULL,
    filename VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    uploaded_by BIGINT REFERENCES members(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_attachments_entity ON attachments(entity_type, entity_id);

-- 답변 템플릿
CREATE TABLE reply_templates (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    content TEXT NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    created_by BIGINT REFERENCES members(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 업무 구독
CREATE TABLE task_watchers (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (task_id, member_id)
);

-- 코멘트
CREATE TABLE task_comments (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES members(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 업무 로그
CREATE TABLE task_logs (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    action VARCHAR(50) NOT NULL,
    actor_id BIGINT REFERENCES members(id) ON DELETE SET NULL,
    details JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_task_logs_task ON task_logs(task_id);

-- 지식 문서 (pgvector 임베딩 포함)
CREATE TABLE knowledge_documents (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(50) NOT NULL,
    source_id VARCHAR(200),
    title VARCHAR(500) NOT NULL,
    content TEXT,
    url VARCHAR(1000),
    tags JSONB,
    embedding vector(768),
    synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_knowledge_source ON knowledge_documents(source, source_id);

-- 지식 커넥터 설정
CREATE TABLE knowledge_connectors (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(50) NOT NULL,
    config JSONB NOT NULL,
    sync_interval_min INT NOT NULL DEFAULT 60,
    last_synced_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
