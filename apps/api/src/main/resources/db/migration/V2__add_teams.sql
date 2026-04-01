-- Phase 2: 팀/멀티 테넌트 지원
-- Version: 2.0

-- 팀
CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 팀 멤버 (다대다 관계)
CREATE TABLE team_members (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    member_id BIGINT NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' CHECK (role IN ('LEADER', 'MEMBER')),
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(team_id, member_id)
);

-- 팀 ID를 문의 테이블에 추가
ALTER TABLE inquiries ADD COLUMN team_id BIGINT REFERENCES teams(id) ON DELETE SET NULL;

-- 팀 ID를 태스크 테이블에 추가
ALTER TABLE tasks ADD COLUMN team_id BIGINT REFERENCES teams(id) ON DELETE SET NULL;

-- 인덱스 생성
CREATE INDEX idx_team_members_team_id ON team_members(team_id);
CREATE INDEX idx_team_members_member_id ON team_members(member_id);
CREATE INDEX idx_inquiries_team_id ON inquiries(team_id);
CREATE INDEX idx_tasks_team_id ON tasks(team_id);

-- 기본 팀 생성 (옵션)
INSERT INTO teams (name, description, is_active) 
VALUES ('General', '일반 지원 팀 (기본)', TRUE);

COMMENT ON TABLE teams IS '팀 (멀티 테넌트 지원)';
COMMENT ON TABLE team_members IS '팀 멤버 관계';
COMMENT ON COLUMN team_members.role IS '팀 내 역할: LEADER(팀 리더), MEMBER(일반 멤버)';
COMMENT ON COLUMN inquiries.team_id IS '담당 팀 (자동 라우팅 또는 수동 배정)';
COMMENT ON COLUMN tasks.team_id IS '담당 팀 (문의에서 상속 또는 수동 배정)';
