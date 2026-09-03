
ALTER TABLE employer_subscriptions
    ADD COLUMN IF NOT EXISTS slots_used integer NOT NULL DEFAULT 0;