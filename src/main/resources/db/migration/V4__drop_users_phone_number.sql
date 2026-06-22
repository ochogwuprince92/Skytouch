-- Remove column added by Hibernate ddl-auto drift (phone lives on job_seekers)
ALTER TABLE users DROP COLUMN IF EXISTS phone_number;
