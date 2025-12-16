--liquibase formatted sql

--changeset kirasin:add-unique-chatid
ALTER TABLE users
    ADD CONSTRAINT uq_users_chat_id UNIQUE (chat_id);