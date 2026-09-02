-- Add comment column to job_applications table
ALTER TABLE job_applications 
ADD COLUMN IF NOT EXISTS comment TEXT;
