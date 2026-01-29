create table "table"
(
    id     serial primary key,
    number int not null unique
);

alter table "order"
    add column if not exists id_table              int,
    add column if not exists installation_datetime timestamp default current_timestamp,
    add column if not exists departure_datetime    timestamp,
    add constraint table_fk foreign key (id_table) references "table" (id) on delete set null;

insert into "table" (id, number)
values (1, 1),
       (2, 2),
       (3, 3),
       (4, 4);

select setval('table_id_seq', (select max(id) from "table"));