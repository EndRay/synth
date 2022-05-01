CREATE TABLE IF NOT EXISTS synths
(
    synth_id integer PRIMARY KEY NOT NULL,

    name     text NOT NULL,
    structure   text NOT NULL,

    UNIQUE (name)
);