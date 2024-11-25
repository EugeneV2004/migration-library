--migration 1--

--migration--
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE
    );

--rollback--
DROP TABLE users;