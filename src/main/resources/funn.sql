-- Database schema for the historical-artifacts-catalog case.
--
-- Design note: the three find types (Mynt, Vapen, Smykke) are modeled as
-- table-per-subclass, mirroring the Java inheritance hierarchy
-- (Funngjenstand -> Mynt/Vapen/Smykke) one-to-one: shared columns live on
-- "funngjenstand", and each subtype's own columns live in its own table,
-- joined back to "funngjenstand" by a shared primary key. That keeps the
-- schema fully normalized (no NULL columns for properties that don't apply
-- to a given type) and keeps the object <-> row mapping in FunnRepository
-- a direct reflection of the class hierarchy instead of a pile of
-- type-specific if-branches over one wide table.
--
-- Written for SQLite (see the README for why: it is a single embedded
-- file, so the project runs in plain IntelliJ with no separate database
-- server to install or configure) but deliberately avoids anything
-- SQLite-specific beyond AUTOINCREMENT, so porting it to
-- MySQL/PostgreSQL later would mean changing type names, not structure.

PRAGMA foreign_keys = ON;

DROP TABLE IF EXISTS mynt;
DROP TABLE IF EXISTS vapen;
DROP TABLE IF EXISTS smykke;
DROP TABLE IF EXISTS funngjenstand;
DROP TABLE IF EXISTS person;
DROP TABLE IF EXISTS museum;

CREATE TABLE person (
    id       INTEGER PRIMARY KEY,
    navn     TEXT NOT NULL,
    telefon  TEXT,
    epost    TEXT
);

CREATE TABLE museum (
    id    INTEGER PRIMARY KEY,
    navn  TEXT NOT NULL,
    sted  TEXT
);

CREATE TABLE funngjenstand (
    id             INTEGER PRIMARY KEY,
    breddegrad     REAL    NOT NULL,
    lengdegrad     REAL    NOT NULL,
    person_id      INTEGER NOT NULL REFERENCES person(id),
    dato_funnet    TEXT    NOT NULL,   -- ISO-8601 (yyyy-MM-dd), matches java.time.LocalDate
    antatt_aar     INTEGER NOT NULL,
    museum_id      INTEGER REFERENCES museum(id),  -- NULL: not (yet) placed in a museum
    type           TEXT    NOT NULL CHECK (type IN ('Mynt', 'Vapen', 'Smykke'))
);

CREATE TABLE mynt (
    funn_id     INTEGER PRIMARY KEY REFERENCES funngjenstand(id) ON DELETE CASCADE,
    diameter_mm REAL NOT NULL,
    metall      TEXT NOT NULL
);

CREATE TABLE vapen (
    funn_id     INTEGER PRIMARY KEY REFERENCES funngjenstand(id) ON DELETE CASCADE,
    vapen_type  TEXT NOT NULL,
    materiale   TEXT NOT NULL,
    vekt_gram   REAL NOT NULL
);

CREATE TABLE smykke (
    funn_id           INTEGER PRIMARY KEY REFERENCES funngjenstand(id) ON DELETE CASCADE,
    smykke_type       TEXT NOT NULL,
    verdiestimat_nok  REAL NOT NULL,
    bildefil          TEXT
);
