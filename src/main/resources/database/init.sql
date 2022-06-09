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

CREATE TABLE IF NOT EXISTS setups
(
    setup_id   integer PRIMARY KEY NOT NULL,

    setup_name text                NOT NULL,

    bpm        real DEFAULT 120 NOT NULL,

    UNIQUE (setup_name)
);

CREATE TABLE IF NOT EXISTS blocks
(
    block_id   integer PRIMARY KEY       NOT NULL,

    setup_id   integer REFERENCES setups NOT NULL,
    x_pos      integer                   NOT NULL,
    y_pos      integer                   NOT NULL,

    block_type text                      NOT NULL
);

CREATE TABLE IF NOT EXISTS synth_blocks_patches
(
    id        integer PRIMARY KEY       NOT NULL,

    block_id  integer REFERENCES blocks NOT NULL,
    synth_id  integer REFERENCES synths NOT NULL,
    polyphony text                      NOT NULL,
    volume    real                      NOT NULL,
    patch_id  integer REFERENCES patches,

    UNIQUE (block_id)
);