-- ─── Users ───────────────────────────────────────────────────────────────────
CREATE TABLE users (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255)    NOT NULL UNIQUE,
    password        VARCHAR(255)    NOT NULL,
    role            VARCHAR(50)     NOT NULL CHECK (role IN ('JOB_SEEKER', 'EMPLOYER', 'ADMIN')),
    status          VARCHAR(50)     NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED')),
    email_verified  BOOLEAN         NOT NULL DEFAULT false,
    active          BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

-- ─── OTP Codes ───────────────────────────────────────────────────────────────
CREATE TABLE otp_codes (
    id          UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id),
    code_hash   VARCHAR(255)    NOT NULL,
    purpose     VARCHAR(50)     NOT NULL CHECK (purpose IN ('EMAIL_VERIFICATION', 'PASSWORD_RESET')),
    attempts    INT             NOT NULL DEFAULT 0,
    expires_at  TIMESTAMP       NOT NULL,
    consumed_at TIMESTAMP,
    created_at  TIMESTAMP       NOT NULL
);

CREATE INDEX idx_otp_codes_user_active
    ON otp_codes(user_id, purpose)
    WHERE consumed_at IS NULL;

-- ─── Auth Sessions ───────────────────────────────────────────────────────────
CREATE TABLE auth_sessions (
    id          UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id),
    token_hash  VARCHAR(64)     NOT NULL UNIQUE,
    expires_at  TIMESTAMP       NOT NULL,
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP       NOT NULL
);

CREATE INDEX idx_auth_sessions_token
    ON auth_sessions(token_hash)
    WHERE revoked_at IS NULL;

-- ─── Companies ───────────────────────────────────────────────────────────────
CREATE TABLE companies (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    industry        VARCHAR(255),
    website         VARCHAR(255),
    logo_url        VARCHAR(255),
    address_line    VARCHAR(255),
    address_lga     VARCHAR(255),
    address_state   VARCHAR(255),
    status          VARCHAR(50)     NOT NULL CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING')),
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

-- ─── Job Seekers ─────────────────────────────────────────────────────────────
CREATE TABLE job_seekers (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_username   VARCHAR(255)    NOT NULL UNIQUE REFERENCES users(email),
    status          VARCHAR(50)     NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED')),
    first_name      VARCHAR(255),
    middle_name     VARCHAR(255),
    last_name       VARCHAR(255),
    phone           VARCHAR(50)     NOT NULL,
    gender          VARCHAR(50),
    birthday        DATE,
    religion        VARCHAR(255),
    nin             VARCHAR(255),
    job             VARCHAR(255),
    qualification   VARCHAR(255),
    cv_url          VARCHAR(255),
    about           TEXT,
    open_to_work    BOOLEAN         NOT NULL DEFAULT false,
    payment_status  VARCHAR(50),
    address_no      VARCHAR(255),
    address_line    VARCHAR(255),
    address_lga     VARCHAR(255),
    address_state   VARCHAR(255),
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

-- ─── Employers ───────────────────────────────────────────────────────────────
CREATE TABLE employers (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_username   VARCHAR(255)    NOT NULL UNIQUE REFERENCES users(email),
    status          VARCHAR(50)     NOT NULL CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED')),
    first_name      VARCHAR(255),
    last_name       VARCHAR(255),
    phone           VARCHAR(50)     NOT NULL,
    company_name    VARCHAR(255),
    job_title       VARCHAR(255),
    company_id      UUID            REFERENCES companies(id),
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

-- ─── Jobs ────────────────────────────────────────────────────────────────────
CREATE TABLE jobs (
    id                  UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id          UUID            NOT NULL REFERENCES companies(id),
    title               VARCHAR(255)    NOT NULL,
    description         TEXT            NOT NULL,
    requirements        TEXT,
    employment_type     VARCHAR(50)     NOT NULL CHECK (employment_type IN ('FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'FREELANCE')),
    work_mode           VARCHAR(50)     NOT NULL CHECK (work_mode IN ('REMOTE', 'ONSITE', 'HYBRID')),
    salary_min          BIGINT,
    salary_max          BIGINT,
    salary_currency     VARCHAR(10)     NOT NULL,
    location_state      VARCHAR(255),
    location_lga        VARCHAR(255),
    status              VARCHAR(50)     NOT NULL CHECK (status IN ('DRAFT', 'PUBLISHED', 'CLOSED', 'SUSPENDED')),
    published_at        TIMESTAMP,
    closed_at           TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP
);

-- ─── Job Applications ────────────────────────────────────────────────────────
CREATE TABLE job_applications (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id          UUID            NOT NULL REFERENCES jobs(id),
    job_seeker_id   UUID            NOT NULL REFERENCES job_seekers(id),
    status          VARCHAR(50)     NOT NULL CHECK (status IN ('PENDING', 'REVIEWING', 'SHORTLISTED', 'INTERVIEWED', 'OFFERED', 'REJECTED', 'WITHDRAWN')),
    cover_letter    TEXT,
    cv_url          VARCHAR(255),
    seeker_name     VARCHAR(255),
    applied_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

-- ─── Interviews ──────────────────────────────────────────────────────────────
CREATE TABLE interviews (
    id                  UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID            NOT NULL UNIQUE REFERENCES job_applications(id),
    scheduled_at        TIMESTAMP       NOT NULL,
    duration_minutes    INT             NOT NULL,
    mode                VARCHAR(50)     NOT NULL CHECK (mode IN ('ONLINE', 'ONSITE', 'PHONE')),
    location_or_link    VARCHAR(255),
    status              VARCHAR(50)     NOT NULL CHECK (status IN ('SCHEDULED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED')),
    notes               TEXT,
    created_by_user_id  UUID            NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP
);

-- ─── Job Offers ──────────────────────────────────────────────────────────────
CREATE TABLE job_offers (
    id                  UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id      UUID            NOT NULL UNIQUE REFERENCES job_applications(id),
    salary_amount       BIGINT,
    salary_currency     VARCHAR(10)     NOT NULL,
    start_date          DATE,
    terms               TEXT,
    status              VARCHAR(50)     NOT NULL CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN', 'EXPIRED')),
    offered_at          TIMESTAMP       NOT NULL,
    expires_at          TIMESTAMP,
    responded_at        TIMESTAMP,
    offered_by_user_id  UUID            NOT NULL REFERENCES users(id),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP
);

-- ─── Saved Jobs ──────────────────────────────────────────────────────────────
CREATE TABLE saved_jobs (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_id   UUID            NOT NULL REFERENCES job_seekers(id),
    job_id          UUID            NOT NULL REFERENCES jobs(id),
    saved_at        TIMESTAMP       NOT NULL,
    CONSTRAINT uq_saved_jobs UNIQUE (job_seeker_id, job_id)
);

-- ─── Job Alerts ──────────────────────────────────────────────────────────────
CREATE TABLE job_alerts (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_id   UUID            NOT NULL REFERENCES job_seekers(id),
    name            VARCHAR(100),
    keyword         VARCHAR(255),
    employment_type VARCHAR(50),
    work_mode       VARCHAR(50),
    location_state  VARCHAR(255),
    industry        VARCHAR(255),
    active          BOOLEAN         NOT NULL DEFAULT true,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP
);

-- ─── Job Alert Digest Deliveries ─────────────────────────────────────────────
CREATE TABLE job_alert_digest_deliveries (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    job_seeker_id   UUID            NOT NULL REFERENCES job_seekers(id),
    job_id          UUID            NOT NULL REFERENCES jobs(id),
    delivered_at    TIMESTAMP       NOT NULL
);

-- ─── Application Messages ────────────────────────────────────────────────────
CREATE TABLE application_messages (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id  UUID            NOT NULL REFERENCES job_applications(id),
    sender_user_id  UUID            NOT NULL REFERENCES users(id),
    body            TEXT            NOT NULL,
    sent_at         TIMESTAMP       NOT NULL,
    read_at         TIMESTAMP
);

-- ─── Notifications ───────────────────────────────────────────────────────────
CREATE TABLE notifications (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID            NOT NULL REFERENCES users(id),
    type            VARCHAR(50)     NOT NULL,
    title           VARCHAR(255)    NOT NULL,
    message         TEXT            NOT NULL,
    application_id  UUID,
    read            BOOLEAN         NOT NULL DEFAULT false,
    created_at      TIMESTAMP       NOT NULL
);

-- ─── Audit Events ────────────────────────────────────────────────────────────
CREATE TABLE audit_events (
    id              UUID            NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_user_id   UUID            NOT NULL REFERENCES users(id),
    action          VARCHAR(50)     NOT NULL,
    target_type     VARCHAR(50)     NOT NULL,
    target_id       UUID            NOT NULL,
    details         TEXT,
    created_at      TIMESTAMP       NOT NULL
);