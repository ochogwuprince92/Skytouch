CREATE TABLE IF NOT EXISTS job_offers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID         NOT NULL UNIQUE REFERENCES job_applications(id),
    salary_amount       BIGINT,
    salary_currency     VARCHAR(10)  NOT NULL DEFAULT 'NGN',
    start_date          DATE,
    terms               TEXT,
    status              VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    offered_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    expires_at          TIMESTAMP,
    responded_at        TIMESTAMP,
    offered_by_user_id  UUID         NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_job_offers_status ON job_offers(status);
