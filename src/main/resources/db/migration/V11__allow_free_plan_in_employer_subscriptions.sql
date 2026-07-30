ALTER TABLE employer_subscriptions
    DROP CONSTRAINT IF EXISTS employer_subscriptions_plan_check;

ALTER TABLE employer_subscriptions
    ADD CONSTRAINT employer_subscriptions_plan_check
    CHECK (plan IN ('FREE', 'BASIC', 'STANDARD', 'PREMIUM'));  -- match your actual enum values + add FREE