#
# --- !Ups

DROP TABLE IF EXISTS translations;

CREATE TABLE translations (
    id serial PRIMARY KEY,
    context text NOT NULL,
    wording text NOT NULL,
    normalization text NOT NULL,
    created_at timestamp with time zone not null default now(),
    updated_at timestamp with time zone not null default now()
);

# --- !Downs

DROP TABLE translations;
