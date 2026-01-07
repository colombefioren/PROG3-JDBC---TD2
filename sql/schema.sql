CREATE TYPE category AS ENUM ('VEGETABLE', 'ANIMAL', 'MARINE', 'DAIRY','OTHER');
CREATE TYPE dish_type AS ENUM ('START', 'MAIN', 'DESSERT');

-- Dish
CREATE TABLE Dish
(
    id        SERIAL PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    dish_type dish_type    NOT NULL
);

-- Ingredient
CREATE TABLE Ingredient
(
    id       SERIAL PRIMARY KEY,
    name     VARCHAR(255)   NOT NULL,
    price    NUMERIC(10, 2) NOT NULL,
    category category       NOT NULL,
    id_dish  INT,
    CONSTRAINT foreign_key_dish
        FOREIGN KEY (id_dish)
            REFERENCES Dish (id)
);

ALTER TABLE dish
    ADD COLUMN IF NOT EXISTS price NUMERIC(10, 2);

UPDATE Dish
SET price = 2000.00
WHERE id = 1; -- Salade de fromage
UPDATE Dish
SET price = 6000.00
WHERE id = 2; -- Poulet grillΘ
UPDATE Dish
SET price = NULL
WHERE id = 3; -- Riz aux lΘgumes
UPDATE Dish
SET price = NULL
WHERE id = 4; -- GΓteau au chocolat
UPDATE Dish
SET price = NULL
WHERE id = 5; -- Salade de fruits
