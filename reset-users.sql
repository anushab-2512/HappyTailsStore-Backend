use ecommerse;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user;
SET FOREIGN_KEY_CHECKS = 1;
INSERT INTO user (user_name, email, password, role, created_at, updated_at) VALUES
('adminuser', 'admin@test.com', '$2b$10$8bSKtSCeMIa9UVmuDKqSROfHXi7GVKREit.5H3ymm8fiP1DqD2aFO', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('customerone', 'customerone@test.com', '$2b$10$8bSKtSCeMIa9UVmuDKqSROfHXi7GVKREit.5H3ymm8fiP1DqD2aFO', 'CUSTOMER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
