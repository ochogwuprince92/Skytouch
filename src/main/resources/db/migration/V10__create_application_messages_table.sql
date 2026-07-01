CREATE TABLE IF NOT EXISTS application_messages (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    application_id  UUID      NOT NULL REFERENCES job_applications(id),
    sender_user_id  UUID      NOT NULL REFERENCES users(id),
    body            TEXT      NOT NULL,
    sent_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_app_messages_application ON application_messages(application_id, sent_at);
