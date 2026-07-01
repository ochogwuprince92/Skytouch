CREATE TABLE IF NOT EXISTS jobs (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id       UUID         NOT NULL REFERENCES companies(id),
    title            VARCHAR(255) NOT NULL,
    description      TEXT         NOT NULL,
    requirements     TEXT,
    employment_type  VARCHAR(50)  NOT NULL,
    work_mode        VARCHAR(50)  NOT NULL,
    salary_min       BIGINT,
    salary_max       BIGINT,
    salary_currency  VARCHAR(10)  NOT NULL DEFAULT 'NGN',
    location_state   VARCHAR(100),
    location_lga     VARCHAR(100),
    status           VARCHAR(50)  NOT NULL DEFAULT 'DRAFT',
    published_at     TIMESTAMP,
    closed_at        TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_jobs_company_id ON jobs(company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON jobs(status);
CREATE INDEX IF NOT EXISTS idx_jobs_employment_type ON jobs(employment_type);
CREATE INDEX IF NOT EXISTS idx_jobs_work_mode ON jobs(work_mode);
CREATE INDEX IF NOT EXISTS idx_jobs_location_state ON jobs(location_state);
