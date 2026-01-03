CREATE TABLE IF NOT EXISTS users (
                                     username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(200) NOT NULL,
    enabled TINYINT(1) NOT NULL
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS authorities (
                                           username VARCHAR(50) NOT NULL,
    authority VARCHAR(50) NOT NULL,
    UNIQUE KEY ix_auth_username (username, authority),
    CONSTRAINT fk_authorities_users
    FOREIGN KEY (username) REFERENCES users(username)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;
