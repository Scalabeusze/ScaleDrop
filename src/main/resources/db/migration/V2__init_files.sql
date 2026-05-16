CREATE TABLE files (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(1024) NOT NULL,
    type VARCHAR(16) NOT NULL CHECK (type IN ('FILE', 'FOLDER')),
    content_type VARCHAR(128),
    size BIGINT NOT NULL,
    hash VARCHAR(255),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING', 'UPLOADED', 'FAILED')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT files_deduplication_unique UNIQUE (owner_id, hash, location, name)
);

CREATE INDEX files_owner_id_idx ON files(owner_id);
