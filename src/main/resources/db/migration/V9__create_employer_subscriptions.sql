-- ─── Employer Subscriptions ─────────────────────────────────────────────────────
CREATE TABLE employer_subscriptions (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id      UUID            NOT NULL REFERENCES companies(id),
    plan            VARCHAR(50)     NOT NULL CHECK (plan IN ('BASIC', 'STANDARD', 'PREMIUM')),
    status          VARCHAR(50)     NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED')),
    start_date      TIMESTAMP       NOT NULL,
    expires_at      TIMESTAMP       NOT NULL,
    billing_cycle   VARCHAR(50)     NOT NULL,
    slots_allocated INTEGER         NOT NULL DEFAULT 0,
    slots_used      INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_employer_subscriptions_company_id ON employer_subscriptions(company_id);
CREATE INDEX idx_employer_subscriptions_status ON employer_subscriptions(status);
