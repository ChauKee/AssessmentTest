CREATE TABLE csv_import
(
    ID           VARCHAR(36) PRIMARY KEY,
    FILE_PATH    VARCHAR(100) NOT NULL,
    STATUS       INTEGER      NOT NULL,
    ERROR        VARCHAR(500),
    CREATED_DATE TIMESTAMP    NOT NULL,
    CREATED_BY   VARCHAR(32)  NOT NULL,
    UPDATED_DATE TIMESTAMP    NOT NULL,
    UPDATED_BY   VARCHAR(32)  NOT NULL
);