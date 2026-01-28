create type category as enum ('VEGETABLE','ANIMAL','MARINE','DAIRY','OTHER');
create type dish_type as enum ('START','MAIN','DESSERT');

create table Dish
(
    id        serial primary key,
    name      varchar(255) not null,
    dish_type dish_type    not null
);

create table Ingredient
(
    id       serial primary key,
    name     varchar(255)   not null,
    price    numeric(10, 2) not null,
    category category       not null,
    id_dish  int,
    constraint dish_fk
        foreign key (id_dish)
            references Dish (id)
);