# JournalEntry schema
 
# --- !Ups

CREATE SEQUENCE user_id_seq;
CREATE TABLE user (
    id integer NOT NULL DEFAULT nextval('user_id_seq'),
    username varchar(255) NOT NULL,
    password varchar(255) NOT NULL,
    last_login timestamp
);
CREATE SEQUENCE journalentry_id_seq;
CREATE TABLE journalentry (
    id integer NOT NULL DEFAULT nextval('journalentry_id_seq'),
    label varchar(255) NOT NULL,
    occurred timestamp NOT NULL,
    comments varchar(255),
    owner varchar(50) NOT NULL,
);
 
# --- !Downs
 
DROP TABLE if exists journalentry;
DROP SEQUENCE if exists journalentry_id_seq;
DROP TABLE if exists user;
DROP SEQUENCE if exists user_id_seq;