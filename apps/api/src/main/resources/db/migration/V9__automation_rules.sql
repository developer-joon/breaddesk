-- Automation Rules
CREATE TABLE automation_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    trigger_type VARCHAR(50) NOT NULL,
    condition_json JSONB NOT NULL,
    action_json JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT check_trigger_type CHECK (trigger_type IN ('INQUIRY_CREATED', 'INQUIRY_STATUS_CHANGED'))
);

CREATE INDEX idx_automation_rules_active ON automation_rules(active);
CREATE INDEX idx_automation_rules_trigger ON automation_rules(trigger_type);

-- Sample data
INSERT INTO automation_rules (name, active, trigger_type, condition_json, action_json) VALUES
('이메일 문의 자동 에스컬레이션', true, 'INQUIRY_CREATED',
 '{"field":"channel","operator":"equals","value":"email"}',
 '{"type":"SET_STATUS","value":"ESCALATED"}'),
('긴급 문의 자동 할당', false, 'INQUIRY_CREATED',
 '{"field":"message","operator":"contains","value":"긴급"}',
 '{"type":"ASSIGN","memberId":1}');
