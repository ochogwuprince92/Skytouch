
ALTER TABLE employer_subscriptions ADD COLUMN slots_used integer DEFAULT 0;

UPDATE employer_subscriptions SET slots_used = 0 WHERE slots_used IS NULL;

ALTER TABLE employer_subscriptions ALTER COLUMN slots_used SET NOT NULL;