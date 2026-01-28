create type movement_type as enum ('OUT','IN');

create table stock_movement
(
    id                serial primary key,
    id_ingredient     int           not null,
    quantity          numeric(10, 2),
    type              movement_type not null,
    unit              unit_type     not null,
    creation_datetime timestamp     not null default current_timestamp,
    constraint ingredient_fk
        foreign key (id_ingredient)
            references ingredient (id)
            on delete cascade
);