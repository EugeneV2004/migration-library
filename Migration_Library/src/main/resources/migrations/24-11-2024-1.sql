--migration 3--

--migration--
ALTER TABLE users
    add column
        year_of_birth int;

--rollback--
ALTER TABLE users
    DROP COLUMN year_of_birth;