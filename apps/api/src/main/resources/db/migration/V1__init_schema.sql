-- BreadDesk 초기 스키마
-- Version: 1.0

-- 팀원
CREATE TABLE members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    role ENUM('AGENT', 'ADMIN') NOT NULL DEFAULT 'AGENT',
    skills JSON,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 문의
CREATE TABLE inquiries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    channel VARCHAR(50) NOT NULL,
    channel_meta JSON,
    sender_name VARCHAR(100) NOT NULL,
    sender_email VARCHAR(200),
    message TEXT NOT NULL,
    ai_response TEXT,
    ai_confidence FLOAT,
    status ENUM('OPEN', 'AI_ANSWERED', 'ESCALATED', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'OPEN',
    task_id BIGINT,
    resolved_by ENUM('AI', 'HUMAN'),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP
);

-- 문의 대화 이력
CREATE TABLE inquiry_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inquiry_id BIGINT NOT NULL,
    role ENUM('USER', 'AI', 'AGENT') NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (inquiry_id) REFERENCES inquiries(id) ON DELETE CASCADE
);

-- SLA 규칙
CREATE TABLE sla_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    urgency ENUM('LOW', 'NORMAL', 'HIGH', 'CRITICAL') NOT NULL UNIQUE,
    response_minutes INT NOT NULL,
    resolve_minutes INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 기본 SLA 규칙 삽입
INSERT INTO sla_rules (urgency, response_minutes, resolve_minutes) VALUES
    ('CRITICAL', 30, 240),
    ('HIGH', 120, 1440),
    ('NORMAL', 240, 4320),
    ('LOW', 1440, 7200);

-- 업무
CREATE TABLE tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    urgency ENUM('LOW', 'NORMAL', 'HIGH', 'CRITICAL') NOT NULL DEFAULT 'NORMAL',
    status ENUM('WAITING', 'IN_PROGRESS', 'REVIEW', 'DONE') NOT NULL DEFAULT 'WAITING',
    requester_name VARCHAR(100),
    requester_email VARCHAR(200),
    assignee_id BIGINT,
    inquiry_id BIGINT,
    ai_summary TEXT,
    due_date DATE,
    estimated_hours FLOAT,
    actual_hours FLOAT,
    sla_response_deadline TIMESTAMP,
    sla_resolve_deadline TIMESTAMP,
    sla_responded_at TIMESTAMP,
    sla_response_breached BOOLEAN NOT NULL DEFAULT FALSE,
    sla_resolve_breached BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    FOREIGN KEY (assignee_id) REFERENCES members(id) ON DELETE SET NULL,
    FOREIGN KEY (inquiry_id) REFERENCES inquiries(id) ON DELETE SET NULL
);

-- inquiries.task_id FK
ALTER TABLE inquiries ADD FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL;

-- 체크리스트
CREATE TABLE task_checklists (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    item_text VARCHAR(500) NOT NULL,
    is_done BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- 업무 태그
CREATE TABLE task_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    tag VARCHAR(100) NOT NULL,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);
CREATE INDEX idx_task_tags_tag ON task_tags(tag);

-- 업무 연결
CREATE TABLE task_relations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_task_id BIGINT NOT NULL,
    target_task_id BIGINT NOT NULL,
    relation_type ENUM('BLOCKS', 'RELATED', 'DUPLICATE') NOT NULL,
    FOREIGN KEY (source_task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (target_task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- 파일 첨부
CREATE TABLE attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type ENUM('INQUIRY', 'TASK') NOT NULL,
    entity_id BIGINT NOT NULL,
    filename VARCHAR(500) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100),
    uploaded_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (uploaded_by) REFERENCES members(id) ON DELETE SET NULL
);
CREATE INDEX idx_attachments_entity ON attachments(entity_type, entity_id);

-- 답변 템플릿
CREATE TABLE reply_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    content TEXT NOT NULL,
    usage_count INT NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES members(id) ON DELETE SET NULL
);

-- 업무 구독
CREATE TABLE task_watchers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
    UNIQUE KEY uk_task_watcher (task_id, member_id)
);

-- 코멘트
CREATE TABLE task_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    author_id BIGINT,
    content TEXT NOT NULL,
    is_internal BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES members(id) ON DELETE SET NULL
);

-- 업무 로그
CREATE TABLE task_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    actor_id BIGINT,
    details JSON,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (actor_id) REFERENCES members(id) ON DELETE SET NULL
);
CREATE INDEX idx_task_logs_task ON task_logs(task_id);

-- 지식 문서
CREATE TABLE knowledge_documents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source VARCHAR(50) NOT NULL,
    source_id VARCHAR(200),
    title VARCHAR(500) NOT NULL,
    content TEXT,
    url VARCHAR(1000),
    tags JSON,
    synced_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_knowledge_source ON knowledge_documents(source, source_id);

-- 지식 커넥터 설정
CREATE TABLE knowledge_connectors (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    source_type VARCHAR(50) NOT NULL,
    config JSON NOT NULL,
    sync_interval_min INT NOT NULL DEFAULT 60,
    last_synced_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
