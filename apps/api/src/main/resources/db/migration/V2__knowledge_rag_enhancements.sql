-- Phase 2: RAG 파이프라인 지원

-- knowledge_documents에 청킹/커넥터 관련 컬럼 추가
ALTER TABLE knowledge_documents ADD COLUMN IF NOT EXISTS connector_id BIGINT REFERENCES knowledge_connectors(id);
ALTER TABLE knowledge_documents ADD COLUMN IF NOT EXISTS chunk_index INT DEFAULT 0;

-- 커넥터 name 컬럼 추가
ALTER TABLE knowledge_connectors ADD COLUMN IF NOT EXISTS name VARCHAR(200);
