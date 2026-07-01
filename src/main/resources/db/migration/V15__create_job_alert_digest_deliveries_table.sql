CREATE TABLE job_alert_digest_deliveries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_id   UUID         NOT NULL REFERENCES job_seekers(id),
    job_id          UUID         NOT NULL REFERENCES jobs(id),
    delivered_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_job_alert_digest_delivery UNIQUE (job_seeker_id, job_id)
);

CREATE INDEX idx_job_alert_digest_deliveries_seeker ON job_alert_digest_deliveries(job_seeker_id);
