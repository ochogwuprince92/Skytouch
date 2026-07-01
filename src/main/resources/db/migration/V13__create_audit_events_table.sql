CREATE TABLE IF NOT EXISTS audit_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    admin_user_id   UUID         NOT NULL REFERENCES users(id),
    action          VARCHAR(50)  NOT NULL,
    target_type     VARCHAR(20)  NOT NULL,
    target_id       UUID         NOT NULL,
    details         TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_events_created_at ON audit_events(created_at DESC);
