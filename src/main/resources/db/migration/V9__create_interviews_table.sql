CREATE TABLE IF NOT EXISTS interviews (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID         NOT NULL UNIQUE REFERENCES job_applications(id),
    scheduled_at        TIMESTAMP    NOT NULL,
    duration_minutes    INTEGER      NOT NULL DEFAULT 60,
    mode                VARCHAR(20)  NOT NULL,
    location_or_link    VARCHAR(500),
    status              VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    notes               TEXT,
    created_by_user_id  UUID         NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_interviews_scheduled_at ON interviews(scheduled_at);
