ALTER TABLE accounts DROP CONSTRAINT accounts_username_key;

CREATE UNIQUE INDEX accounts_username_non_disabled_uniq_idx
    ON accounts(username)
    WHERE status <> 'DISABLED';