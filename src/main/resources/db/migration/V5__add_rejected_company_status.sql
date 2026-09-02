-- ─── Add REJECTED status to companies table ─────────────────────────────────────
-- This migration allows companies to be rejected during admin moderation

-- Drop existing constraint
ALTER TABLE companies DROP CONSTRAINT IF EXISTS companies_status_check;

-- Add constraint with REJECTED included
ALTER TABLE companies 
ADD CONSTRAINT companies_status_check 
CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING', 'REJECTED'));
