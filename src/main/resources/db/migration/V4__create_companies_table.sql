CREATE TABLE IF NOT EXISTS companies (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    industry      VARCHAR(255),
    website       VARCHAR(500),
    logo_url      VARCHAR(500),
    address_line  VARCHAR(500),
    address_lga   VARCHAR(100),
    address_state VARCHAR(100),
    status        VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP
);

ALTER TABLE employers
    ADD COLUMN IF NOT EXISTS company_id UUID REFERENCES companies(id);

CREATE INDEX IF NOT EXISTS idx_employers_company_id ON employers(company_id);
