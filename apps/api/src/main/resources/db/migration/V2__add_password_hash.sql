-- Add passwordHash column to members table
-- Phase 1 코드리뷰 P0 이슈 수정

ALTER TABLE members ADD COLUMN password_hash VARCHAR(255) NOT NULL DEFAULT '';
