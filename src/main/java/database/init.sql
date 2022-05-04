CREATE TABLE IF NOT EXISTS synths
(
    synth_id   integer PRIMARY KEY NOT NULL,

    synth_name text                NOT NULL,
    structure  text                NOT NULL,

    UNIQUE (synth_name)
);

CREATE TABLE IF NOT EXISTS patches
(
    patch_id   integer PRIMARY KEY       NOT NULL,

    patch_name text                      NOT NULL,
    synth_id   integer REFERENCES synths NOT NULL,

    UNIQUE (patch_name, synth_id)
);

CREATE TABLE IF NOT EXISTS parameters
(
    id             integer PRIMARY KEY        NOT NULL,

    parameter_name text                       NOT NULL,
    value          real                       NOT NULL,

    patch_id       integer REFERENCES patches NOT NULL,

    UNIQUE (parameter_name, patch_id)
);