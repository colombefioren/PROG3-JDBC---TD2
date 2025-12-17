CREATE DATABASE mini_dish_db;

\c mini_dish_db

CREATE ROLE mini_dish_db_manager_role NOLOGIN;

CREATE SCHEMA mini_dish_db_management AUTHORIZATION mini_dish_db_manager_role;

CREATE USER mini_dish_db_manager WITH PASSWORD '123456';
GRANT mini_dish_db_manager_role TO mini_dish_db_manager;

GRANT CONNECT ON DATABASE mini_dish_db TO mini_dish_db_manager;

GRANT USAGE, CREATE ON SCHEMA mini_dish_db_management TO mini_dish_db_manager_role;

GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES
    IN SCHEMA mini_dish_db_management TO mini_dish_db_manager_role;

GRANT USAGE, SELECT ON ALL SEQUENCES
    IN SCHEMA mini_dish_db_management TO mini_dish_db_manager_role;

SET ROLE mini_dish_db_manager_role;

ALTER DEFAULT PRIVILEGES IN SCHEMA mini_dish_db_management
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO mini_dish_db_manager_role;

ALTER DEFAULT PRIVILEGES IN SCHEMA mini_dish_db_management
    GRANT USAGE, SELECT ON SEQUENCES TO mini_dish_db_manager_role;

RESET ROLE;

ALTER ROLE mini_dish_db_manager SET search_path = mini_dish_db_management;

\c product_management_db product_manager_user
