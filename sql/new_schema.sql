create type unit_type as enum ('KG', 'L', 'PCS');

-- create a dish_ingredient table
create table dish_ingredient
(
    id                serial primary key,
    id_dish           int            not null,
    id_ingredient     int            not null,
    quantity_required numeric(10, 2) not null,
    unit              unit_type      not null,

    constraint fk_dish
        foreign key (id_dish)
            references Dish (id)
            on delete cascade,
    constraint fk_ingredient
        foreign key (id_ingredient)
            references Ingredient (id)
            on delete cascade,
    constraint unique_dish_ingredient
        unique (id_dish, id_ingredient)
);

-- rename price to selling price if exists
ALTER TABLE Dish
    RENAME COLUMN price TO selling_price;

-- add selling_price if it doesnt
ALTER TABLE Dish
    ADD COLUMN IF NOT EXISTS selling_price NUMERIC;

-- delete id_dish column in ingredient since it's not necessary anymore
ALTER TABLE ingredient
    DROP COLUMN IF EXISTS id_dish;
