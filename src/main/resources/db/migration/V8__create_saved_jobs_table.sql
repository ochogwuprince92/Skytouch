CREATE TABLE IF NOT EXISTS saved_jobs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_id  UUID      NOT NULL REFERENCES job_seekers(id),
    job_id         UUID      NOT NULL REFERENCES jobs(id),
    saved_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (job_seeker_id, job_id)
);

CREATE INDEX IF NOT EXISTS idx_saved_jobs_seeker ON saved_jobs(job_seeker_id);
