-- ─── Employer Subscriptions ─────────────────────────────────────────────────────
CREATE TABLE employer_subscriptions (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    employer_id     UUID            NOT NULL REFERENCES employers(id),
    plan            VARCHAR(50)     NOT NULL CHECK (plan IN ('BASIC', 'STANDARD', 'PREMIUM')),
    status          VARCHAR(50)     NOT NULL CHECK (status IN ('ACTIVE', 'EXPIRED', 'CANCELLED')),
    start_date      DATE            NOT NULL,
    end_date        DATE            NOT NULL,
    slots_allocated INTEGER         NOT NULL DEFAULT 0,
    slots_used      INTEGER         NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_employer_subscriptions_employer_id ON employer_subscriptions(employer_id);
CREATE INDEX idx_employer_subscriptions_status ON employer_subscriptions(status);
