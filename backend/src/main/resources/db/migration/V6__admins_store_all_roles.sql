-- Admins store every lower role explicitly (no role hierarchy).
-- Give existing ADMIN users the USER and MODERATOR rows they do not have yet.
INSERT INTO user_roles (user_id, role)
SELECT admin.user_id, lower_role.role
FROM user_roles admin
CROSS JOIN (VALUES ('USER'), ('MODERATOR')) AS lower_role (role)
WHERE admin.role = 'ADMIN'
ON CONFLICT (user_id, role) DO NOTHING;
