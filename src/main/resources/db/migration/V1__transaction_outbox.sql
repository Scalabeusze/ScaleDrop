CREATE TABLE txno_version
(
    id      integer not null primary key default 0,
    version integer
);

CREATE TABLE txno_sequence
(
    topic varchar(250) not null,
    seq   bigint       not null,
    primary key (topic, seq)
);

CREATE TABLE txno_outbox
(
    id              varchar(36) PRIMARY KEY           NOT NULL,
    invocation      text,
    nextattempttime timestamp(6),
    attempts        int,
    blocked         boolean,
    version         int,
    uniquerequestid varchar(250),
    processed       boolean,
    lastAttemptTime timestamp(6),
    topic           VARCHAR(250) default '*'::varchar not null,
    seq             bigint
);

CREATE INDEX idx_txno_outbox ON txno_outbox (processed, blocked, nextattempttime);
CREATE INDEX idx_txno_outbox_topic ON txno_outbox (topic, processed, seq);
