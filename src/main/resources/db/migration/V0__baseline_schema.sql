CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(50)  NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    email_verified  BOOLEAN      NOT NULL DEFAULT FALSE,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE TABLE IF NOT EXISTS job_seekers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_username   VARCHAR(255) NOT NULL UNIQUE REFERENCES users(email),
    status          VARCHAR(50)  NOT NULL,
    first_name      VARCHAR(255),
    middle_name     VARCHAR(255),
    last_name       VARCHAR(255),
    phone           VARCHAR(255) NOT NULL,
    gender          VARCHAR(50),
    birthday        DATE,
    religion        VARCHAR(100),
    nin             VARCHAR(50),
    job             VARCHAR(255),
    qualification   VARCHAR(255),
    cv_url          VARCHAR(500),
    about           TEXT,
    open_to_work    BOOLEAN      NOT NULL DEFAULT FALSE,
    payment_status  VARCHAR(50),
    address_no      VARCHAR(100),
    address_line    VARCHAR(500),
    address_lga     VARCHAR(100),
    address_state   VARCHAR(100),
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_job_seekers_user_username ON job_seekers(user_username);

CREATE TABLE IF NOT EXISTS otp_codes (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID         NOT NULL REFERENCES users(id),
    code_hash   VARCHAR(255) NOT NULL,
    purpose     VARCHAR(50)  NOT NULL,
    attempts    INTEGER      NOT NULL DEFAULT 0,
    expires_at  TIMESTAMP    NOT NULL,
    consumed_at TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_otp_codes_user_id ON otp_codes(user_id);
