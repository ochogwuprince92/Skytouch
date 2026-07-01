CREATE TABLE IF NOT EXISTS job_alerts (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_id    UUID         NOT NULL REFERENCES job_seekers(id),
    name             VARCHAR(100),
    keyword          VARCHAR(255),
    employment_type  VARCHAR(20),
    work_mode        VARCHAR(20),
    location_state   VARCHAR(100),
    industry         VARCHAR(100),
    active           BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_job_alerts_seeker ON job_alerts(job_seeker_id);
CREATE INDEX IF NOT EXISTS idx_job_alerts_active ON job_alerts(active);
