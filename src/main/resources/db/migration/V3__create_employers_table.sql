CREATE TABLE IF NOT EXISTS employers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_username   VARCHAR(255) NOT NULL UNIQUE REFERENCES users(email),
    status          VARCHAR(50)  NOT NULL,
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    phone           VARCHAR(50)  NOT NULL,
    company_name    VARCHAR(255),
    job_title       VARCHAR(255),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_employers_user_username ON employers(user_username);
