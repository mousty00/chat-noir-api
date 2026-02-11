-- Insert default user roles
INSERT INTO user_role (name, description) VALUES
                                                  -- system roles
                                                  ( 'ADMIN', 'System administrator with full access to all features and data'),
                                                  ( 'USER', 'Regular user with basic access to view and favorite cats'),
                                                  ('PREMIUM', 'Premium user with extended API access and features'),

                                                  -- content management roles
                                                  ('MODERATOR', 'Content moderator who can review and moderate cat submissions'),
                                                  ( 'CONTENT_CREATOR', 'User who can submit new cat content for review'),

                                                  -- api developer roles
                                                  ( 'DEVELOPER', 'Developer with API access for integration purposes'),
                                                  ( 'PARTNER', 'Partner organization with special access');