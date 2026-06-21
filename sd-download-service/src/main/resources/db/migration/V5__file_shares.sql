CREATE TABLE file_shares
(
    id      UUID PRIMARY KEY,
    file_id UUID NOT NULL,
    from_id UUID NOT NULL,
    to_id   UUID NOT NULL,
    CONSTRAINT fk_file_shares_file_id FOREIGN KEY (file_id) REFERENCES files (id),
    CONSTRAINT uq_file_shares_file_id_from_id_to_id UNIQUE (file_id, from_id, to_id)
);

CREATE INDEX idx_file_shares_from_id ON file_shares (from_id);
CREATE INDEX idx_file_shares_to_id ON file_shares (to_id);
