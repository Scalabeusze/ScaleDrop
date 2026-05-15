CREATE TABLE files
(
    id            UUID PRIMARY KEY,
    key           TEXT                     NOT NULL UNIQUE,
    size          BIGINT                   NOT NULL,
    last_modified TIMESTAMP WITH TIME ZONE NOT NULL,
    e_tag         VARCHAR(255)             NOT NULL
);

CREATE INDEX idx_files_owner_id ON files (owner_id);
