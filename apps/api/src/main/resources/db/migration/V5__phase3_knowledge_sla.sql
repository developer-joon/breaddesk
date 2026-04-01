-- Phase 3: Knowledge Enhancement + SLA
-- Migration for knowledge base auto-accumulation and SLA improvements

-- No schema changes needed for knowledge auto-accumulation (uses existing tables)
-- Knowledge documents already support source="inquiry"

-- Add index for faster knowledge source lookups
CREATE INDEX IF NOT EXISTS idx_knowledge_documents_source_source_id 
ON knowledge_documents(source, source_id);

-- Ensure SLA fields exist on tasks (should already exist from V1)
-- Adding comments for clarity
COMMENT ON COLUMN tasks.sla_response_deadline IS 'SLA deadline for first response';
COMMENT ON COLUMN tasks.sla_resolve_deadline IS 'SLA deadline for resolution';
COMMENT ON COLUMN tasks.sla_responded_at IS 'Actual first response time';
COMMENT ON COLUMN tasks.sla_response_breached IS 'Whether response SLA was breached';
COMMENT ON COLUMN tasks.sla_resolve_breached IS 'Whether resolve SLA was breached';

-- Ensure inquiry-task linking exists
COMMENT ON COLUMN inquiries.task_id IS 'Linked task when inquiry is escalated';
