-- Phase 2: 업무관리 강화
-- 1. 팀(Team) 테이블 생성
-- 2. 팀-멤버 조인 테이블
-- 3. Task에 team_id 추가
-- 4. Inquiry에 team_id 추가

-- 팀 테이블
CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 팀-멤버 조인 테이블
CREATE TABLE team_members (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    role VARCHAR(50) DEFAULT 'MEMBER', -- LEADER, MEMBER
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(team_id, member_id)
);

CREATE INDEX idx_team_members_team_id ON team_members(team_id);
CREATE INDEX idx_team_members_member_id ON team_members(member_id);

-- Task에 담당팀 추가
ALTER TABLE tasks ADD COLUMN team_id BIGINT REFERENCES teams(id) ON DELETE SET NULL;
CREATE INDEX idx_tasks_team_id ON tasks(team_id);

-- Inquiry에 담당팀 추가
ALTER TABLE inquiries ADD COLUMN team_id BIGINT REFERENCES teams(id) ON DELETE SET NULL;
CREATE INDEX idx_inquiries_team_id ON inquiries(team_id);

-- 기본 팀 데이터 (예시)
INSERT INTO teams (name, description) VALUES
    ('IT 인프라팀', 'VPN, 서버, 네트워크 관련'),
    ('개발팀', '기능 개발, 버그 수정'),
    ('운영팀', '일반 문의 및 사용자 지원');
