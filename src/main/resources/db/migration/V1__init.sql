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

CREATE TABLE IF NOT EXISTS forum_users(
    nickname CITEXT NOT NULL references users(nickname),
    forum CITEXT references forums(slug)
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
    path INT ARRAY NOT NULL
);

CREATE TABLE IF NOT EXISTS votes(
    nickname CITEXT references users(nickname),
    voice INT NOT NULL,
    id SERIAL PRIMARY KEY,
    thread INT references threads(id)
);

DROP INDEX IF EXISTS idx_post_parent_thread;
CREATE INDEX IF NOT EXISTS idx_post_parent_thread ON posts(parent,thread);
DROP INDEX IF EXISTS idx_post_path;
CREATE INDEX IF NOT EXISTS idx_post_path ON posts(path);
DROP INDEX IF EXISTS idx_post_id;
CREATE INDEX IF NOT EXISTS idx_post_id ON posts(id);
DROP INDEX IF EXISTS idx_post_path1;
CREATE INDEX IF NOT EXISTS idx_post_path1 ON posts((path[1]));

DROP INDEX IF EXISTS idx_forums_slug;
CREATE INDEX IF NOT EXISTS idx_forums_slug ON forums(slug);
DROP INDEX IF EXISTS idx_threads_forum;
CREATE INDEX IF NOT EXISTS idx_threads_forum ON threads(forum);
DROP INDEX IF EXISTS idx_posts_forum;
CREATE INDEX IF NOT EXISTS idx_posts_forum ON posts(forum);
DROP INDEX IF EXISTS idx_threads_author;
CREATE INDEX IF NOT EXISTS idx_threads_author ON threads(author);
DROP INDEX IF EXISTS idx_posts_author;
CREATE INDEX IF NOT EXISTS idx_posts_author ON posts(author);

DROP INDEX IF EXISTS idx_users_nickname;
CREATE INDEX IF NOT EXISTS idx_users_nickname ON users(nickname);

DROP INDEX IF EXISTS idx_forum_users_forum;
CREATE INDEX IF NOT EXISTS idx_forum_users_forum ON forum_users(forum);
DROP INDEX IF EXISTS idx_forum_users_nickname;
CREATE INDEX IF NOT EXISTS idx_forum_users_nickname ON forum_users(nickname);
