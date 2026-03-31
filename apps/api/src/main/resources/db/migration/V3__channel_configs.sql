-- V3: Channel configuration for webhook integration
CREATE TABLE channel_configs (
    id              BIGSERIAL PRIMARY KEY,
    channel_type    VARCHAR(50) NOT NULL UNIQUE,
    webhook_url     VARCHAR(500),
    auth_token      VARCHAR(500),
    is_active       BOOLEAN NOT NULL DEFAULT true,
    config          JSONB DEFAULT '{}',
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP
);

CREATE INDEX idx_channel_configs_type ON channel_configs (channel_type);
CREATE INDEX idx_channel_configs_active ON channel_configs (is_active);

-- Seed default channels
INSERT INTO channel_configs (channel_type, is_active, config) VALUES
    ('slack', false, '{"displayName": "Slack", "icon": "slack"}'),
    ('teams', false, '{"displayName": "Microsoft Teams", "icon": "teams"}'),
    ('email', false, '{"displayName": "Email", "icon": "email"}');
