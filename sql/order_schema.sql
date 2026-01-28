create table "order"
(
    id                serial primary key,
    reference         varchar(8) not null,
    creation_datetime timestamp  not null default current_timestamp
);

create table dish_order
(
    id       serial primary key,
    id_order int not null,
    id_dish  int not null,
    quantity int not null,
    constraint order_fk
        foreign key (id_order)
            references "order" (id)
            on delete cascade,
    constraint dish_fk
        foreign key (id_dish)
            references Dish (id)
            on delete cascade
);