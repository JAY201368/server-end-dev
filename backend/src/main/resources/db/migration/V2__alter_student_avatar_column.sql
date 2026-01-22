-- Migration: Alter student avatar column to support base64 image data
-- Date: 2026-01-22
-- Reason: VARCHAR(255) is too small for base64-encoded images

ALTER TABLE student
MODIFY COLUMN avatar MEDIUMTEXT COMMENT '头像数据 - base64编码或URL';
