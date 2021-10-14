create table if not exists post (
    id serial primary key,
    title varchar (256),
    description text,
    link varchar (256) unique,
    created timestamp
);