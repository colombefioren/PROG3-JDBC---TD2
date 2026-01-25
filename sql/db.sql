create database revisionfour;

create user revisionfour_user with password  '123456';

\c revisionfour;

grant connect on database revisionfour to revisionfour_user;

grant create on schema public to revisionfour_user;

alter default privileges in schema public grant select, insert, update, delete on tables to revisionfour_user;

alter default privileges in schema public grant usage, select, update on sequences to revisionfour_user;