CREATE TABLE todo(
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL
);

INSERT INTO todo (title)
VALUES
    ('scala study'),
    ('ZIO study');