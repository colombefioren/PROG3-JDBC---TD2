alter table Dish
    add column if not exists selling_price numeric(10, 2);

update Dish
set selling_price = 2000
where id = 1;

update Dish
set selling_price = 6000
where id = 2;