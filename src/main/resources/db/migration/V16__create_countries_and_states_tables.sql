CREATE TABLE IF NOT EXISTS countries (
    id              INTEGER PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    iso3            VARCHAR(3),
    iso2            VARCHAR(2),
    numeric_code    VARCHAR(10),
    phone_code      VARCHAR(20),
    capital         VARCHAR(255),
    currency        VARCHAR(10),
    currency_name   VARCHAR(100),
    currency_symbol VARCHAR(20),
    tld             VARCHAR(20),
    native          VARCHAR(255),
    region          VARCHAR(100),
    subregion       VARCHAR(100),
    latitude        DECIMAL(12, 8),
    longitude       DECIMAL(12, 8),
    emoji           VARCHAR(20),
    has_states      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS states (
    id              INTEGER PRIMARY KEY,
    country_id      INTEGER      NOT NULL REFERENCES countries(id),
    name            VARCHAR(255) NOT NULL,
    state_code      VARCHAR(20),
    has_cities      BOOLEAN      NOT NULL DEFAULT FALSE,
    latitude        DECIMAL(12, 8),
    longitude       DECIMAL(12, 8)
);

CREATE INDEX IF NOT EXISTS idx_countries_iso2 ON countries(iso2);
CREATE INDEX IF NOT EXISTS idx_countries_name ON countries(name);
CREATE INDEX IF NOT EXISTS idx_states_country_id ON states(country_id);
CREATE INDEX IF NOT EXISTS idx_states_name ON states(name);
