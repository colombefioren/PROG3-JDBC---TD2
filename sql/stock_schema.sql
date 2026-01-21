create type movement_type as enum ('IN','OUT');

create table stock_movement
(
    id            serial primary key,
    id_ingredient int                                 not null,
    quantity      int                                 not null,
    unit          unit_type                           not null,
    datetime      timestamp default current_timestamp not null,
    type          movement_type                       not null,
    constraint fk_id_ing foreign key (id_ingredient)
        references Ingredient (id)
        on delete cascade
);