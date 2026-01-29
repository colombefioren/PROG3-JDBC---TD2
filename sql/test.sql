insert into "order" (id, reference, installation_datetime, departure_datetime, id_table)
values (1, 'ORD101', '2025-01-29 11:00', null, 1),
       (2, 'ORD102', '2025-01-29 10:00', '2025-01-29 10:15', 2),
       (3, 'ORD103', '2025-01-29 11:25', '2025-01-29 11:40', 3);


insert into dish_order (quantity, id_order, id_dish)
values (1, 1, 1),
       (1, 2, 2),
       (1, 3, 1);
