CREATE TABLE IF NOT EXISTS job_applications (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id         UUID         NOT NULL REFERENCES jobs(id),
    job_seeker_id  UUID         NOT NULL REFERENCES job_seekers(id),
    status         VARCHAR(50)  NOT NULL DEFAULT 'SUBMITTED',
    cover_letter   TEXT,
    cv_url         VARCHAR(500),
    seeker_name    VARCHAR(255),
    applied_at     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP,
    CONSTRAINT uq_job_applications_job_seeker UNIQUE (job_id, job_seeker_id)
);

CREATE INDEX IF NOT EXISTS idx_job_applications_job_id ON job_applications(job_id);
CREATE INDEX IF NOT EXISTS idx_job_applications_job_seeker_id ON job_applications(job_seeker_id);
CREATE INDEX IF NOT EXISTS idx_job_applications_status ON job_applications(status);
