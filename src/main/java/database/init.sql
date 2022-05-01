CREATE TABLE IF NOT EXISTS synths
(
    name      text,
    structure text,

    synth_id  serial PRIMARY KEY,

    UNIQUE (name)
);