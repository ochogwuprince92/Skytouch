-- Update jobs.status to use CHECK constraint
-- First, clean up any existing invalid data
UPDATE jobs 
SET status = 'DRAFT' 
WHERE status NOT IN ('DRAFT', 'ACTIVE', 'CLOSED');

ALTER TABLE jobs 
DROP CONSTRAINT IF EXISTS jobs_status_check;

ALTER TABLE jobs 
ADD CONSTRAINT jobs_status_check 
CHECK (status IN ('DRAFT', 'ACTIVE', 'CLOSED'));
