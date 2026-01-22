create table "order"
(
    id                serial primary key,
    reference         varchar(8) not null,
    creation_datetime timestamp  not null default current_timestamp
);

create table dish_order
(
    id       serial primary key,
    quantity int not null,
    id_order int,
    id_dish  int,
    constraint fk_order
        foreign key (id_order)
            references "order" (id)
            on delete cascade,
    constraint fk_dish
        foreign key (id_dish)
            references dish (id)
            on delete cascade
);
