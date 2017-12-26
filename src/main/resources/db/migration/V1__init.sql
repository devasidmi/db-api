CREATE EXTENSION IF NOT EXISTS CITEXT;

CREATE TABLE IF NOT EXISTS users(
    about CITEXT,
    email CITEXT NOT NULL UNIQUE,
    fullname CITEXT,
    nickname CITEXT NOT NULL PRIMARY KEY
);

CREATE TABLE  IF NOT EXISTS forums(
    posts INT DEFAULT 0,
    slug CITEXT NOT NULL PRIMARY KEY,
    threads INT DEFAULT 0,
    title CITEXT NOT NULL,
    "user" CITEXT references users(nickname)
);

CREATE TABLE IF NOT EXISTS threads(
    author CITEXT references users(nickname),
    created TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    forum CITEXT references forums(slug),
    id SERIAL PRIMARY KEY,
    message CITEXT NOT NULL,
    slug CITEXT UNIQUE,
    title CITEXT NOT NULL,
    votes INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS posts(
    author CITEXT references users(nickname),
    created TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    forum CITEXT references forums(slug),
    id SERIAL PRIMARY KEY,
    isEdited bool DEFAULT false NOT NULL,
    message CITEXT NOT NULL,
    parent INT NOT NULL DEFAULT 0,
    thread INT references threads(id),
    path INT[] NOT NULL
);

CREATE TABLE IF NOT EXISTS votes(
    nickname CITEXT references users(nickname),
    voice INT NOT NULL,
    id SERIAL PRIMARY KEY,
    thread CITEXT references threads(slug)
);
