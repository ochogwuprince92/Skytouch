-- Update job_applications.status to match Java ApplicationStatus enum
-- First, clean up any existing invalid data by mapping old values to new ones
UPDATE job_applications 
SET status = 'SUBMITTED' 
WHERE status = 'PENDING';

UPDATE job_applications 
SET status = 'INTERVIEW_SCHEDULED' 
WHERE status = 'INTERVIEWED';

UPDATE job_applications 
SET status = 'OFFER_EXTENDED' 
WHERE status = 'OFFERED';

-- Drop the old constraint
ALTER TABLE job_applications 
DROP CONSTRAINT IF EXISTS job_applications_status_check;

-- Add the new constraint matching the Java enum
ALTER TABLE job_applications 
ADD CONSTRAINT job_applications_status_check 
CHECK (status IN (
    'SUBMITTED', 
    'REVIEWING', 
    'SHORTLISTED', 
    'INTERVIEW_SCHEDULED', 
    'OFFER_EXTENDED', 
    'OFFER_DECLINED', 
    'HIRED', 
    'REJECTED', 
    'WITHDRAWN'
));
