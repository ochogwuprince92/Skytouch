CREATE TABLE otp_codes (
    id           UUID         NOT NULL PRIMARY KEY,
    user_id      UUID         NOT NULL REFERENCES users (id),
    code_hash    VARCHAR(255) NOT NULL,
    purpose      VARCHAR(50)  NOT NULL DEFAULT 'LOGIN',
    attempts     INT          NOT NULL DEFAULT 0,
    expires_at   TIMESTAMP    NOT NULL,
    consumed_at  TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_otp_codes_user_active ON otp_codes (user_id, purpose)
    WHERE consumed_at IS NULL;

CREATE TABLE auth_sessions (
    id           UUID         NOT NULL PRIMARY KEY,
    user_id      UUID         NOT NULL REFERENCES users (id),
    token_hash   VARCHAR(64)  NOT NULL UNIQUE,
    expires_at   TIMESTAMP    NOT NULL,
    revoked_at   TIMESTAMP,
    created_at   TIMESTAMP    NOT NULL
);

CREATE INDEX idx_auth_sessions_token ON auth_sessions (token_hash)
    WHERE revoked_at IS NULL;
