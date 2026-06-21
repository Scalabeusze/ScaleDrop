CREATE TABLE identities(
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id),
    provider VARCHAR(32) NOT NULL CHECK (provider IN ('LOCAL', 'GOOGLE')),
    provider_subject VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    email_verified BOOLEAN NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT identities_provider_subject_unique UNIQUE (provider, provider_subject)
);

CREATE INDEX identities_account_id_idx ON identities(account_id);
