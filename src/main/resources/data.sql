INSERT INTO user_role (name, description) VALUES
    ('ADMIN',           'System administrator with full access to all features and data'),
    ('USER',            'Regular user with basic access to view and favorite cats'),
    ('PREMIUM',         'Premium user with extended API access and features'),
    ('MODERATOR',       'Content moderator who can review and moderate cat submissions'),
    ('CONTENT_CREATOR', 'User who can submit new cat content for review'),
    ('DEVELOPER',       'Developer with API access for integration purposes'),
    ('PARTNER',         'Partner organization with special access')
ON CONFLICT DO NOTHING;
