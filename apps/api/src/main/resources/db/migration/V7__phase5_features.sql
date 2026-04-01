-- Phase 5: Advanced Features
-- CSAT Surveys, Prompt Configs, Analytics support

-- CSAT Survey table
CREATE TABLE IF NOT EXISTS csat_surveys (
    id BIGSERIAL PRIMARY KEY,
    inquiry_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    feedback TEXT,
    sent_at TIMESTAMP NOT NULL,
    responded_at TIMESTAMP,
    responded BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (inquiry_id) REFERENCES inquiries(id) ON DELETE CASCADE
);

CREATE INDEX idx_csat_surveys_inquiry_id ON csat_surveys(inquiry_id);
CREATE INDEX idx_csat_surveys_token ON csat_surveys(token);
CREATE INDEX idx_csat_surveys_responded ON csat_surveys(responded);

COMMENT ON TABLE csat_surveys IS 'Customer satisfaction surveys sent after inquiry resolution';
COMMENT ON COLUMN csat_surveys.token IS 'Public access token for survey (UUID)';
COMMENT ON COLUMN csat_surveys.rating IS '1-5 star rating';

-- Prompt Configuration table
CREATE TABLE IF NOT EXISTS prompt_configs (
    id BIGSERIAL PRIMARY KEY,
    key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    prompt_template TEXT NOT NULL,
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_prompt_configs_key ON prompt_configs(key);
CREATE INDEX idx_prompt_configs_active ON prompt_configs(active);

COMMENT ON TABLE prompt_configs IS 'AI prompt templates for different features';
COMMENT ON COLUMN prompt_configs.key IS 'Unique key identifier (e.g., ai_answer, classification)';
COMMENT ON COLUMN prompt_configs.prompt_template IS 'Prompt template with placeholders';

-- Add sentiment field to inquiry_messages if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'inquiry_messages' AND column_name = 'sentiment'
    ) THEN
        ALTER TABLE inquiry_messages ADD COLUMN sentiment VARCHAR(20);
        CREATE INDEX idx_inquiry_messages_sentiment ON inquiry_messages(sentiment);
        COMMENT ON COLUMN inquiry_messages.sentiment IS 'POSITIVE, NEUTRAL, NEGATIVE, ANGRY';
    END IF;
END $$;

-- Insert default prompt configs
INSERT INTO prompt_configs (key, name, prompt_template, description, active, created_at, updated_at)
VALUES 
('ai_answer', 'AI Auto Answer', 
'You are a helpful customer support AI. Answer the following question based on the provided knowledge base.\n\nQuestion: {question}\n\nKnowledge: {knowledge}\n\nAnswer:', 
'Default prompt for AI auto-answer feature', 
TRUE, NOW(), NOW()),

('classification', 'Category Classification', 
'Classify the following customer inquiry into one of these categories: TECHNICAL, BILLING, GENERAL, COMPLAINT.\n\nInquiry: {message}\n\nCategory:', 
'Prompt for automatic category classification', 
TRUE, NOW(), NOW()),

('sentiment', 'Sentiment Analysis', 
'Analyze the sentiment of the following message. Respond with ONLY ONE WORD: POSITIVE, NEUTRAL, NEGATIVE, or ANGRY.\n\nMessage: {message}\n\nSentiment:', 
'Prompt for sentiment analysis', 
TRUE, NOW(), NOW()),

('summary', 'Conversation Summary', 
'Summarize the following customer support conversation in 2-3 sentences.\n\nConversation: {conversation}\n\nSummary:', 
'Prompt for generating conversation summaries', 
TRUE, NOW(), NOW())
ON CONFLICT (key) DO NOTHING;
