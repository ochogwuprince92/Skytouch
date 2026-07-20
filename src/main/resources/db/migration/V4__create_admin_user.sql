INSERT INTO users (
    id,
    email,
    password,
    role,
    status,
    email_verified,
    active,
    created_at,
    updated_at
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'admin.skytouch@gmail.com',
    'Ogwaismywife@2018',
    'ADMIN',
    'ACTIVE',
    true,
    true,
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;
