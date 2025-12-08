--liquibase formatted sql

--changeset kirasin:add-role-column
ALTER TABLE users
    ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER';

--changeset kirasin:add-role-check-constraint
ALTER TABLE users
    ADD CONSTRAINT chk_user_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'));