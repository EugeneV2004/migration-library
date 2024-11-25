--migration 2--

--migration--
ALTER TABLE users
    add column
        address varchar;

--rollback--
ALTER TABLE users
    DROP COLUMN address;