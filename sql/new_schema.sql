create type unit_type as enum ('PCS','KG','L');

create table dish_ingredient
(
    id                serial primary key,
    id_dish           int            not null,
    id_ingredient     int            not null,
    quantity_required numeric(10, 2) not null,
    unit              unit_type      not null,
    constraint dish_fk
        foreign key (id_dish)
            references Dish (id)
            on delete cascade,
    constraint ingredient_fk
        foreign key (id_ingredient)
            references ingredient (id)
            on delete cascade,
    constraint dish_ingredient_unique
        unique (id_dish, id_ingredient)
);

alter table ingredient
    drop column id_dish;