--liquibase formatted sql

--changeset kirasin:add-password-column
ALTER TABLE users
    ADD COLUMN password VARCHAR(60) NOT NULL DEFAULT '$2a$10$dummy...';