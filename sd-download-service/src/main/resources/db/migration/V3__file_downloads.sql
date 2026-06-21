CREATE TABLE file_downloads
(
    id           UUID PRIMARY KEY,
    file_id      UUID                     NOT NULL,
    requested_at TIMESTAMP WITH TIME ZONE NOT NULL,
    expires_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_file_downloads_file_id FOREIGN KEY (file_id) REFERENCES files (id)
);

CREATE INDEX idx_file_downloads_file_id ON file_downloads (file_id);
