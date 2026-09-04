-- ─── Payments ─────────────────────────────────────────────────────
CREATE TABLE payments (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    reference       VARCHAR(255)    NOT NULL UNIQUE,
    company_id      UUID            NOT NULL REFERENCES companies(id),
    amount          NUMERIC(19,2)   NOT NULL,
    currency        VARCHAR(3)      NOT NULL,
    status          VARCHAR(50)     NOT NULL,
    gateway_response TEXT,
    paid_at         TIMESTAMP,
    metadata        TEXT,
    customer_email  VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

CREATE INDEX idx_payments_company_id ON payments(company_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_reference ON payments(reference);
