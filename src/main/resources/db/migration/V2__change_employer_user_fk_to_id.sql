-- Change Employer.user foreign key from email-based to ID-based
-- This aligns with the JPA entity mapping change from user_username/email to user_id/id

-- Step 1: Add new user_id column (nullable initially)
ALTER TABLE employers 
ADD COLUMN user_id UUID REFERENCES users(id);

-- Step 2: Populate user_id based on existing user_username (email) values
UPDATE employers e
SET user_id = u.id
FROM users u
WHERE e.user_username = u.email;

-- Step 3: Make user_id NOT NULL after data is populated
ALTER TABLE employers 
ALTER COLUMN user_id SET NOT NULL;

-- Step 4: Add UNIQUE constraint to user_id
ALTER TABLE employers 
ADD CONSTRAINT uq_employers_user_id UNIQUE (user_id);

-- Step 5: Drop the old foreign key constraint on user_username
ALTER TABLE employers 
DROP CONSTRAINT employers_user_username_fkey;

-- Step 6: Drop the old UNIQUE constraint on user_username
ALTER TABLE employers 
DROP CONSTRAINT employers_user_username_key;

-- Step 7: Drop the old user_username column
ALTER TABLE employers 
DROP COLUMN user_username;
