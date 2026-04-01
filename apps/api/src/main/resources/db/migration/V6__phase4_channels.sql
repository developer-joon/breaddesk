-- Phase 4: Omnichannel Support
-- Migration for unified channel messages

-- Create channel_messages table for omnichannel support
CREATE TABLE IF NOT EXISTS channel_messages (
    id BIGSERIAL PRIMARY KEY,
    channel_type VARCHAR(20) NOT NULL,
    source VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    sender_info JSONB,
    channel_metadata JSONB,
    inquiry_id BIGINT REFERENCES inquiries(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP,
    processed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_channel_messages_processed ON channel_messages(processed, created_at);
CREATE INDEX idx_channel_messages_inquiry_id ON channel_messages(inquiry_id);
CREATE INDEX idx_channel_messages_channel_type ON channel_messages(channel_type);

COMMENT ON TABLE channel_messages IS 'Unified incoming messages from all channels';
COMMENT ON COLUMN channel_messages.channel_type IS 'EMAIL, WEB_CHAT, KAKAO, TELEGRAM, WEBHOOK';
COMMENT ON COLUMN channel_messages.source IS 'Channel-specific identifier (e.g., slack:C12345)';
COMMENT ON COLUMN channel_messages.sender_info IS 'JSON: {name, email, userId}';
COMMENT ON COLUMN channel_messages.channel_metadata IS 'Channel-specific metadata for replies';
