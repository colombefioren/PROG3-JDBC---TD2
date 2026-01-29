package com.jdbctd2.repository;

import com.jdbctd2.db.DBConnection;
import com.jdbctd2.model.*;
import com.jdbctd2.model.enums.CategoryEnum;
import com.jdbctd2.model.enums.DishTypeEnum;
import com.jdbctd2.model.enums.MovementTypeEnum;
import com.jdbctd2.model.enums.UnitType;
import com.jdbctd2.repository.interf.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataRetriever
    implements IngredientRepository,
        DishRepository,
        DishIngredientRepository,
        StockMovementRepository,
        OrderRepository,
        DishOrderRepository,
        TableRepository {
  private final DBConnection dbConnection;

  public DataRetriever() {
    this.dbConnection = new DBConnection();
  }

  // initialize db

  public void initializeDB() {
    String eraseDataSql =
        """
                    truncate dish_ingredient, dish, ingredient restart identity cascade;
            """;
    String ingredientDataSql =
        """
                    insert into Ingredient (id, name, price, category) values
                        (1, 'Laitue', 800.00, 'VEGETABLE'),
                        (2, 'Tomate', 600.00, 'VEGETABLE'),
                        (3, 'Poulet', 4500.00, 'ANIMAL'),
                        (4, 'Chocolat', 3000.00, 'OTHER'),
                        (5, 'Beurre', 2500.00, 'DAIRY');
            """;

    String dishDataSql =
        """
                  insert into Dish (id, name, dish_type,selling_price) values
                        (1, 'Salade fraîche', 'START',3500.00),
                        (2, 'Poulet grillé', 'MAIN',12000.00),
                        (3, 'Riz aux légumes', 'MAIN',null),
                        (4, 'Gâteau au chocolat', 'DESSERT',8000.00),
                        (5, 'Salade de fruits', 'DESSERT',null);
            """;

    String dishIngSql =
        """
            insert into dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit)
            values (1, 1, 1, 0.20, 'KG'),
                   (2, 1, 2, 0.15, 'KG'),
                   (3, 2, 3, 1.00, 'KG'),
                   (4, 4, 4, 0.30, 'KG'),
                   (5, 4, 5, 0.20, 'KG');
            """;

    String stockDataSql =
        """
            insert into stock_movement (id_ingredient, quantity, unit, creation_datetime, type)
            values (1, 5.0, 'KG', '2024-01-05 08:00', 'IN'),
                   (1, 2.0, 'PCS', '2024-01-06 12:00', 'OUT'),
                   (2, 4.0, 'KG', '2024-01-05 08:00', 'IN'),
                   (2, 5.0, 'PCS', '2024-01-06 12:00', 'OUT'),
                   (3, 10.0, 'KG', '2024-01-04 09:00', 'IN'),
                   (3, 4.0, 'PCS', '2024-01-06 13:00', 'OUT'),
                   (4, 3.0, 'KG', '2024-01-05 10:00', 'IN'),
                   (4, 1.0, 'L', '2024-01-06 14:00', 'OUT'),
                   (5, 2.5, 'KG', '2024-01-05 10:00', 'IN'),
                   (5, 1.0, 'L', '2024-01-06 14:00', 'OUT');
            """;

    String dishIngSqSql =
        """
            select setval('dish_ingredient_id_seq',(select max(id) from dish_ingredient));
            """;

    String dishSqSql =
        """
                    select setval('dish_id_seq', (select max(id) from Dish));
            """;

    String ingredientSqSql =
        """
            select setval('ingredient_id_seq', (select max(id) from Ingredient));
        """;

    String stockSqSql =
        """
            select setval('stock_movement_id_seq', (select max(id) from stock_movement));

            """;
    Connection con = null;
    Statement stmt = null;

    try {
      con = dbConnection.getDBConnection();
      stmt = con.createStatement();
      stmt.executeUpdate(eraseDataSql);
      stmt.executeUpdate(dishDataSql);
      stmt.executeUpdate(ingredientDataSql);
      stmt.executeUpdate(dishIngSql);
      stmt.executeUpdate(stockDataSql);
      stmt.executeQuery(dishSqSql);
      stmt.executeQuery(ingredientSqSql);
      stmt.executeQuery(dishIngSqSql);
      stmt.executeQuery(stockSqSql);
    } catch (SQLException e) {
      throw new RuntimeException("Error while initializing db ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(stmt, con);
    }
  }

  // Dish methods

  @Override
  public Dish findDishById(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    String dishSql =
        """
                select d.id as d_id, d.name as d_name, d.dish_type, d.selling_price as d_price from dish d where d.id = ?
            """;

    Connection con = null;
    PreparedStatement dishStmt = null;
    ResultSet dishRs = null;

    try {
      con = dbConnection.getDBConnection();
      dishStmt = con.prepareStatement(dishSql);
      dishStmt.setInt(1, id);

      dishRs = dishStmt.executeQuery();

      if (!dishRs.next()) {
        throw new RuntimeException("Dish with id " + id + " not found");
      }

      return mapDishFromResultSet(dishRs);
    } catch (SQLException e) {
      throw new RuntimeException("Error while trying to fetch dish ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(dishRs, dishStmt, con);
    }
  }

  @Override
  public Dish saveDish(Dish dish) {
    if (dish == null) {
      throw new IllegalArgumentException("Dish cannot be null");
    }

    isValid(dish);

    String saveDishSql =
        """
                    insert into dish (id, name, dish_type, selling_price)
                    values (?, ?, ?::dish_type, ?)
                    on conflict (id) do update
                    set name = excluded.name, dish_type = excluded.dish_type, selling_price = excluded.selling_price
                    returning id
                """;

    Connection con = null;
    PreparedStatement saveDishStmt = null;
    ResultSet saveDishRs = null;
    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      saveDishStmt = con.prepareStatement(saveDishSql);
      if (dish.getId() == null) {
        saveDishStmt.setInt(1, getNextSerialValue(con, "Dish", "id"));
      } else {
        saveDishStmt.setInt(1, dish.getId());
      }
      saveDishStmt.setString(2, dish.getName());
      saveDishStmt.setString(3, dish.getDishType().name());
      if (dish.getPrice() == null) {
        saveDishStmt.setNull(4, Types.DOUBLE);
      } else {
        saveDishStmt.setDouble(4, dish.getPrice());
      }
      saveDishRs = saveDishStmt.executeQuery();
      saveDishRs.next();
      int dishId = saveDishRs.getInt(1);

      List<DishIngredient> newDishIngredients = dish.getDishIngredients();
      detachIngredients(con, dishId, newDishIngredients);
      attachIngredients(con, dishId, newDishIngredients);

      con.commit();
      return findDishById(
          dishId); // find the dish with the relations and all since attach and detach ingredient
      // already created the needed dishIngredient with all fields
    } catch (SQLException e) {
      rollbackQuietly(con);
      throw new RuntimeException("Failed to save new dish ", e);
    } catch (RuntimeException e) {
      rollbackQuietly(con);
      throw new RuntimeException("Failed to save dish ", e);
    } finally {
      restoreAutoCommit(con);
      dbConnection.attemptCloseDBConnection(saveDishRs, saveDishStmt, con);
    }
  }

  @Override
  public List<Dish> findDishesByIngredientName(String ingredientName) {
    if (ingredientName == null || ingredientName.isBlank()) {
      throw new IllegalArgumentException("Ingredient name cannot be null or empty");
    }

    String findDishSql =
        """
                    select d.id as d_id, d.name as d_name, d.dish_type, d.selling_price as d_price,
                    i.name as i_name from dish d
                    join dish_ingredient di on di.id_dish = d.id
                    join ingredient i on i.id = di.id_ingredient where i.name ilike ?
                """;

    Connection con = null;
    PreparedStatement findDishStmt = null;
    ResultSet findDishRs = null;

    try {
      con = dbConnection.getDBConnection();
      findDishStmt = con.prepareStatement(findDishSql);
      findDishStmt.setString(1, "%" + ingredientName + "%");
      findDishRs = findDishStmt.executeQuery();
      List<Dish> dishes = new ArrayList<>();
      while (findDishRs.next()) {
        dishes.add(mapDishFromResultSet(findDishRs));
      }
      return dishes;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to fetch dishes by ingredient name ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findDishRs, findDishStmt, con);
    }
  }

  private void verifyDishExists(Connection con, Integer dishId) {
    if (dishId == null) {
      throw new IllegalArgumentException("Dish id cannot be null");
    }

    String verifyDishExistenceSql = "select 1 from dish where id = ?";
    PreparedStatement verifyDishExistenceStmt = null;
    ResultSet verifyDishExistenceRs = null;

    try {
      verifyDishExistenceStmt = con.prepareStatement(verifyDishExistenceSql);
      verifyDishExistenceStmt.setInt(1, dishId);
      verifyDishExistenceRs = verifyDishExistenceStmt.executeQuery();
      if (!verifyDishExistenceRs.next()) {
        throw new RuntimeException("Dish with id " + dishId + " does not exist");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Failed to verify if dish exists ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(verifyDishExistenceRs, verifyDishExistenceStmt);
    }
  }

  // Ingredient methods

  @Override
  public List<Ingredient> findIngredients(int page, int size) {
    if (page <= 0 || size <= 0) {
      throw new IllegalArgumentException("page and size must be positive and greater than 0");
    }

    String findIngSql =
        """
                    select i.id as i_id, i.name as i_name, i.price as i_price, i.category as i_category
                    from ingredient i
                    order by i.id
                    limit ? offset ?
                """;

    Connection con = null;
    PreparedStatement findIngStmt = null;
    ResultSet findIngRs = null;
    int offset = (page - 1) * size;

    try {
      con = dbConnection.getDBConnection();
      findIngStmt = con.prepareStatement(findIngSql);
      findIngStmt.setInt(1, size);
      findIngStmt.setInt(2, offset);
      findIngRs = findIngStmt.executeQuery();

      List<Ingredient> ingredients = new ArrayList<>();
      while (findIngRs.next()) {
        ingredients.add(mapIngredientFromResultSet(findIngRs));
      }

      return ingredients;

    } catch (SQLException e) {
      throw new RuntimeException("Error while trying to fetch ingredients ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findIngRs, findIngStmt, con);
    }
  }

  @Override
  public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
    if (newIngredients == null || newIngredients.isEmpty()) {
      throw new IllegalArgumentException("New ingredients list cannot be null or empty");
    }

    for (Ingredient newIngredient : newIngredients) {
      if (newIngredient == null) {
        throw new IllegalArgumentException("New ingredient cannot be null");
      }
      isValid(newIngredient);
    }

    String createIngSql =
        """
                    insert into ingredient (id, name, price, category)
                    values (?, ?, ?, ?::category)
                    returning id
                """;

    Connection con = null;
    PreparedStatement createIngStmt = null;
    ResultSet createIngRs = null;
    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      createIngStmt = con.prepareStatement(createIngSql);
      List<Ingredient> createdIngredients = new ArrayList<>();

      for (Ingredient newIngredient : newIngredients) {
        if (newIngredient.getId() == null) {
          createIngStmt.setInt(1, getNextSerialValue(con, "Ingredient", "id"));
        } else {
          createIngStmt.setInt(1, newIngredient.getId());
        }
        createIngStmt.setString(2, newIngredient.getName());
        createIngStmt.setDouble(3, newIngredient.getPrice());
        createIngStmt.setString(4, newIngredient.getCategory().name());
        createIngRs = createIngStmt.executeQuery();
        createIngRs.next();
        newIngredient.setId(createIngRs.getInt(1));
        createdIngredients.add(newIngredient);
      }
      con.commit();
      return createdIngredients;
    } catch (SQLException e) {
      rollbackQuietly(con);
      throw new RuntimeException("Error while creating ingredients ", e);
    } catch (RuntimeException e) {
      rollbackQuietly(con);
      throw new RuntimeException("Failed to create ingredients ", e);
    } finally {
      restoreAutoCommit(con);
      dbConnection.attemptCloseDBConnection(createIngRs, createIngStmt, con);
    }
  }

  @Override
  public List<Ingredient> findIngredientsByCriteria(
      String ingredientName, CategoryEnum category, String dishName, int page, int size) {

    if (page <= 0 || size <= 0) {
      throw new IllegalArgumentException("Page and size must be valid values");
    }

    String findIngSql = getFindIngSql(ingredientName, category, dishName);

    Connection con = null;
    PreparedStatement findIngStmt = null;
    ResultSet findIngRs = null;

    try {
      con = dbConnection.getDBConnection();
      findIngStmt = con.prepareStatement(findIngSql);
      int paramIndex = 1;

      if (ingredientName != null && !ingredientName.isBlank()) {
        findIngStmt.setString(paramIndex++, "%" + ingredientName + "%");
      }

      if (category != null) {
        findIngStmt.setString(paramIndex++, category.name());
      }

      if (dishName != null && !dishName.isBlank()) {
        findIngStmt.setString(paramIndex++, "%" + dishName + "%");
      }

      findIngStmt.setInt(paramIndex++, size);
      findIngStmt.setInt(paramIndex, (page - 1) * size);

      findIngRs = findIngStmt.executeQuery();
      List<Ingredient> ingredients = new ArrayList<>();
      while (findIngRs.next()) {
        ingredients.add(mapIngredientFromResultSet(findIngRs));
      }
      return ingredients;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to fetch ingredients by criteria ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findIngRs, findIngStmt, con);
    }
  }

  @Override
  public Ingredient findIngredientByName(String ingredientName) {
    String findIngByNameSql =
        """
                    select i.id as i_id, i.name as i_name, i.price as i_price, i.category as i_category
                    from ingredient i
                    where lower(i.name) = lower(?)
                    order by i_id
                    """;

    Connection con = null;
    PreparedStatement findIngByNameStmt = null;
    ResultSet findIngByNameRs = null;

    try {
      con = dbConnection.getDBConnection();
      findIngByNameStmt = con.prepareStatement(findIngByNameSql);
      findIngByNameStmt.setString(1, ingredientName);
      findIngByNameRs = findIngByNameStmt.executeQuery();
      if (!findIngByNameRs.next()) {
        return null;
      }
      return mapIngredientFromResultSet(findIngByNameRs);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find ingredient by its name ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findIngByNameRs, findIngByNameStmt, con);
    }
  }

  @Override
  public Ingredient findIngredientById(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }
    String findIng =
        """
                    select i.id as i_id, i.name as i_name, i.price as i_price, i.category as i_category from ingredient i where i.id = ?
                """;

    Connection con = null;
    PreparedStatement findIngStmt = null;
    ResultSet findIngRs = null;
    try {
      con = dbConnection.getDBConnection();
      findIngStmt = con.prepareStatement(findIng);
      findIngStmt.setInt(1, id);
      findIngRs = findIngStmt.executeQuery();
      if (!findIngRs.next()) {
        throw new RuntimeException("Ingredient not found");
      }
      return mapIngredientFromResultSet(findIngRs);
    } catch (SQLException e) {
      throw new RuntimeException("Error while fetching ingredient ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findIngRs, findIngStmt, con);
    }
  }

  @Override
  public Ingredient saveIngredient(Ingredient toSave) {
    if (toSave == null) {
      throw new IllegalArgumentException("Ingredient cannot be null");
    }
    isValid(toSave);

    String saveIngSql =
        """
                insert into ingredient (id, name, price, category)
                values (?, ?, ?, ?::category)
                on conflict (id) do update set name = excluded.name, price = excluded.price, category = excluded.category
                returning id
            """;

    Connection con = null;
    PreparedStatement saveIngStmt = null;
    ResultSet saveIngRs = null;
    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      saveIngStmt = con.prepareStatement(saveIngSql);
      if (toSave.getId() == null) {
        saveIngStmt.setInt(1, getNextSerialValue(con, "ingredient", "id"));
      } else {
        saveIngStmt.setInt(1, toSave.getId());
      }
      saveIngStmt.setString(2, toSave.getName());
      saveIngStmt.setDouble(3, toSave.getPrice());
      saveIngStmt.setString(4, toSave.getCategory().name());
      saveIngRs = saveIngStmt.executeQuery();
      saveIngRs.next();
      Integer savedIngredientId = saveIngRs.getInt(1);

      List<StockMovement> toSaveStockMovementList = toSave.getStockMovementList();
      if (toSaveStockMovementList != null && !toSaveStockMovementList.isEmpty()) {
        for (StockMovement stockMovement : toSaveStockMovementList) {
          saveStockMovement(con, stockMovement, savedIngredientId);
        }
      }
      con.commit();
      return findIngredientById(savedIngredientId);
    } catch (SQLException e) {
      rollbackQuietly(con);
      throw new RuntimeException("Error while saving ingredient ", e);
    } catch (RuntimeException e) {
      rollbackQuietly(con);
      throw new RuntimeException("Failed to create the new ingredient ", e);
    } finally {
      restoreAutoCommit(con);
      dbConnection.attemptCloseDBConnection(saveIngRs, saveIngStmt, con);
    }
  }

  private Ingredient saveIngredient(Connection con, Ingredient ingredient) {
    if (ingredient == null) {
      throw new IllegalArgumentException("Ingredient cannot be null");
    }

    isValid(ingredient);

    String saveIngSql =
        "insert into ingredient (id, name, price, category) values (?, ?, ?, ?::category) returning id";
    PreparedStatement saveIngStmt = null;
    ResultSet saveIngRs = null;
    try {
      saveIngStmt = con.prepareStatement(saveIngSql);
      if (ingredient.getId() == null) {
        saveIngStmt.setInt(1, getNextSerialValue(con, "ingredient", "id"));
      } else {
        saveIngStmt.setInt(1, ingredient.getId());
      }
      saveIngStmt.setString(2, ingredient.getName());

      saveIngStmt.setDouble(3, ingredient.getPrice());
      saveIngStmt.setString(4, ingredient.getCategory().name());
      saveIngRs = saveIngStmt.executeQuery();
      saveIngRs.next();
      return mapIngredientFromResultSet(saveIngRs);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to save ingredient ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(saveIngRs, saveIngStmt);
    }
  }

  private void verifyIngredientExistence(Connection con, Integer ingredientId) {
    if (ingredientId == null) {
      throw new IllegalArgumentException("Ingredient id cannot be null");
    }

    String verifyIngExistenceSql = "select 1 from ingredient where id = ?";
    PreparedStatement verifyIngExistenceStmt = null;
    ResultSet verifyIngExistenceRs = null;
    try {
      verifyIngExistenceStmt = con.prepareStatement(verifyIngExistenceSql);
      verifyIngExistenceStmt.setInt(1, ingredientId);
      verifyIngExistenceRs = verifyIngExistenceStmt.executeQuery();
      if (!verifyIngExistenceRs.next()) {
        throw new RuntimeException("Ingredient with id " + ingredientId + " does not exist");
      }
    } catch (SQLException e) {
      throw new RuntimeException(
          "An error occurred while trying to verify ingredient existence ", e);
    }
  }

  // DishIngredient methods

  @Override
  public List<DishIngredient> findDishIngredientsByDishId(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    String dishIngredientSql =
        """
                  select di.id as di_id, di.id_dish, di.id_ingredient, di.quantity_required as di_quantity_required, di.unit as di_unit from dish_ingredient di where di.id_dish = ?
                """;

    Connection con = null;
    PreparedStatement dishIngStmt = null;
    ResultSet dishIngRs = null;

    try {
      con = dbConnection.getDBConnection();
      dishIngStmt = con.prepareStatement(dishIngredientSql);
      dishIngStmt.setInt(1, id);
      dishIngRs = dishIngStmt.executeQuery();

      List<DishIngredient> dishIngredients = new ArrayList<>();
      while (dishIngRs.next()) {
        dishIngredients.add(mapDishIngredientFromResultSet(dishIngRs));
      }

      return dishIngredients;

    } catch (SQLException e) {
      throw new RuntimeException("Error while fetching dish ingredients ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(dishIngRs, dishIngStmt, con);
    }
  }

  @Override
  public DishIngredient saveDishIngredient(DishIngredient newDishIngredient) {
    if (newDishIngredient == null) {
      throw new IllegalArgumentException("DishIngredient cannot be null");
    }

    isValid(newDishIngredient);

    String saveDishIngSql =
        """
                insert into dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit)
                values (?, ?, ?, ?, ?::unit_type)
                returning id
            """;

    Connection con = null;
    PreparedStatement saveDishIngStmt = null;
    ResultSet saveDishIngRs = null;

    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      saveDishIngStmt = con.prepareStatement(saveDishIngSql);

      if (newDishIngredient.getId() == null) {
        saveDishIngStmt.setInt(1, getNextSerialValue(con, "dish_ingredient", "id"));
      } else {
        saveDishIngStmt.setInt(1, newDishIngredient.getId());
      }
      saveDishIngStmt.setInt(2, newDishIngredient.getDish().getId());
      saveDishIngStmt.setInt(3, newDishIngredient.getIngredient().getId());
      saveDishIngStmt.setDouble(4, newDishIngredient.getQuantityRequired());
      saveDishIngStmt.setString(5, newDishIngredient.getUnit().name());

      saveDishIngRs = saveDishIngStmt.executeQuery();
      saveDishIngRs.next();

      con.commit();

      return findDishIngredientById(saveDishIngRs.getInt(1));

    } catch (SQLException e) {
      rollbackQuietly(con);
      throw new RuntimeException("An error occurred while trying to save new dish ingredient ", e);
    } catch (RuntimeException e) {
      rollbackQuietly(con);
      throw new RuntimeException("Failed to save new dish ingredient ", e);
    } finally {
      restoreAutoCommit(con);
      dbConnection.attemptCloseDBConnection(saveDishIngRs, saveDishIngStmt, con);
    }
  }

  @Override
  public DishIngredient findDishIngredientById(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    String findDishIngSql =
        """
                select di.id as di_id, di.id_dish, di.id_ingredient, di.quantity_required as di_quantity_required, di.unit as di_unit from dish_ingredient di where di.id = ?
            """;

    Connection con = null;
    PreparedStatement findDishIngStmt = null;
    ResultSet findDishIngRs = null;

    try {
      con = dbConnection.getDBConnection();
      findDishIngStmt = con.prepareStatement(findDishIngSql);
      findDishIngStmt.setInt(1, id);
      findDishIngRs = findDishIngStmt.executeQuery();
      if (!findDishIngRs.next()) {
        throw new RuntimeException("DishIngredient not found");
      }
      DishIngredient dishIngredient = mapDishIngredientFromResultSet(findDishIngRs);
      dishIngredient.setDish(findDishById(dishIngredient.getDish().getId()));
      return dishIngredient;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find dish ingredient by id ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findDishIngRs, findDishIngStmt, con);
    }
  }

  // StockMovement methods

  private void saveStockMovement(
      Connection con, StockMovement stockMovement, Integer ingredientId) {
    if (stockMovement == null || ingredientId == null) {
      throw new IllegalArgumentException("StockMovement and ingredientId cannot be null");
    }

    isValid(stockMovement);

    String saveStockSql =
        """
                insert into stock_movement
                (id, id_ingredient, quantity, type, unit, creation_datetime)
                values (?, ?, ?, ?::movement_type,?,?)
                on conflict (id) do nothing
            """;

    PreparedStatement saveStockStmt = null;
    try {
      saveStockStmt = con.prepareStatement(saveStockSql);
      if (stockMovement.getId() == null) {
        saveStockStmt.setInt(1, getNextSerialValue(con, "stock_movement", "id"));
      } else {
        saveStockStmt.setInt(1, stockMovement.getId());
      }
      saveStockStmt.setInt(2, ingredientId);
      saveStockStmt.setDouble(3, stockMovement.getValue().getQuantity());
      saveStockStmt.setString(4, stockMovement.getType().name());
      saveStockStmt.setString(5, stockMovement.getValue().getUnit().name());
      if (stockMovement.getCreationDatetime() == null) {
        saveStockStmt.setTimestamp(6, Timestamp.from(Instant.now()));
      } else {
        saveStockStmt.setTimestamp(6, Timestamp.from(stockMovement.getCreationDatetime()));
      }
      saveStockStmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to save new stock movement ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(saveStockStmt);
    }
  }

  @Override
  public StockMovement findStockMovementById(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    String findStockSql =
        """
      select st.id as st_id, st.id_ingredient,
      st.quantity as st_quantity, st.type as st_type,
      st.unit as st_unit, st.creation_datetime
      as st_creation_datetime from stock_movement st where id = ?
      """;

    Connection con = null;
    PreparedStatement findStockStmt = null;
    ResultSet findStockRs = null;
    try {
      con = dbConnection.getDBConnection();
      findStockStmt = con.prepareStatement(findStockSql);
      findStockStmt.setInt(1, id);
      findStockRs = findStockStmt.executeQuery();
      if (!findStockRs.next()) {
        throw new RuntimeException("StockMovement not found");
      }
      return mapStockMovementFromResultSet(findStockRs);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find stock movement ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findStockRs, findStockStmt, con);
    }
  }

  @Override
  public List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId) {
    if (ingredientId == null) {
      throw new IllegalArgumentException("ingredientId cannot be null");
    }

    String findStocksSql =
        """
                select st.id as st_id, st.id_ingredient, st.quantity as st_quantity, st.type as st_type, st.unit as st_unit, st.creation_datetime as st_creation_datetime from stock_movement st where id_ingredient = ? order by st_creation_datetime desc
            """;

    Connection con = null;
    PreparedStatement findStocksStmt = null;
    ResultSet findStocksRs = null;

    try {
      con = dbConnection.getDBConnection();
      findStocksStmt = con.prepareStatement(findStocksSql);
      findStocksStmt.setInt(1, ingredientId);
      findStocksRs = findStocksStmt.executeQuery();
      List<StockMovement> stockMovements = new ArrayList<>();
      while (findStocksRs.next()) {
        stockMovements.add(mapStockMovementFromResultSet(findStocksRs));
      }
      return stockMovements;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to fetch stock movements of ingredient ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findStocksRs, findStocksStmt, con);
    }
  }

  private void validateStockForOrder(Order orderToSave) {
    for (DishOrder dishOrder : orderToSave.getDishOrders()) {
      Dish dish =
          findDishById(
              dishOrder
                  .getDish()
                  .getId()); // finding the dish allows us to take the dish with its ingredients
      // stock movements
      for (DishIngredient dishIngredient : dish.getDishIngredients()) {
        Ingredient ingredient = dishIngredient.getIngredient();

        double required =
            ingredient.convertToKG(
                dishIngredient.getQuantityRequired() * dishOrder.getQuantity(),
                dishIngredient.getUnit());

        StockValue currentStock = ingredient.getStockValueAt(Instant.now());

        if (currentStock.getQuantity() < required) {
          throw new RuntimeException("Not enough stock for ingredient " + ingredient.getName());
        }
      }
    }
  }

  private void updateStock(Connection con, Order order) {
    if (order == null) {
      throw new IllegalArgumentException("Order cannot be null");
    }

    String updateStockSql =
        """
                    insert into stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
                    values (?, ?, ?, 'OUT'::movement_type, ?::unit_type, ?)
                """;

    PreparedStatement updateStockStmt = null;

    try {
      updateStockStmt = con.prepareStatement(updateStockSql);

      for (DishOrder dishOrder : order.getDishOrders()) {
        Dish dish = findDishById(dishOrder.getDish().getId());

        for (DishIngredient dishIngredient : dish.getDishIngredients()) {
          double quantityToRemove = dishIngredient.getQuantityRequired() * dishOrder.getQuantity();

          updateStockStmt.setInt(1, getNextSerialValue(con, "stock_movement", "id"));
          updateStockStmt.setInt(2, dishIngredient.getIngredient().getId());
          updateStockStmt.setDouble(3, quantityToRemove);
          updateStockStmt.setString(4, dishIngredient.getUnit().name());
          updateStockStmt.setTimestamp(5, Timestamp.from(Instant.now()));

          updateStockStmt.executeUpdate();
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException("An error occurred while trying to update stock ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(updateStockStmt);
    }
  }

  // order methods

  @Override
  public Order saveOrder(Order orderToSave) {
    if (orderToSave == null) {
      throw new IllegalArgumentException("Order cannot be null");
    }

    isValid(orderToSave);

    if (orderToSave.getTableOrder() == null || orderToSave.getTableOrder().getTable() == null) {
      throw new IllegalArgumentException("TableOrder with Table must be specified for the order");
    }

    Table table = orderToSave.getTableOrder().getTable();
    Instant arrivalTime = orderToSave.getTableOrder().getArrivalDatetime();

    if (arrivalTime == null) {
      // Si pas d'heure d'arrivée spécifiée, utiliser l'heure de création
      arrivalTime = orderToSave.getCreationDatetime();
      orderToSave.getTableOrder().setArrivalDatetime(arrivalTime);
    }

    checkTableAvailability(table, arrivalTime);

    String orderSql =
        """
                 insert into "order" (id, reference, creation_datetime, id_table, installation_datetime, departure_datetime)\s
                            values (?, ?, ?, ?, ?, ?)
                            returning id
            """;

    validateStockForOrder(orderToSave);
    Connection con = null;
    PreparedStatement orderStmt = null;
    ResultSet orderRs = null;
    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);

      orderStmt = con.prepareStatement(orderSql);
      if (orderToSave.getId() == null) {
        orderStmt.setInt(1, getNextSerialValue(con, "\"order\"", "id"));
      } else {
        orderStmt.setInt(1, orderToSave.getId());
      }
      orderStmt.setString(2, orderToSave.getReference());

      if (orderToSave.getCreationDatetime() == null) {
        orderStmt.setTimestamp(3, Timestamp.from(Instant.now()));
      } else {
        orderStmt.setTimestamp(3, Timestamp.from(orderToSave.getCreationDatetime()));
      }

      // Ajouter les informations de table
      orderStmt.setInt(4, table.getId());
      orderStmt.setTimestamp(5, Timestamp.from(arrivalTime));

      Instant departureTime = orderToSave.getTableOrder().getDepartureDatetime();
      if (departureTime != null) {
        orderStmt.setTimestamp(6, Timestamp.from(departureTime));
      } else {
        orderStmt.setNull(6, Types.TIMESTAMP);
      }

      orderRs = orderStmt.executeQuery();
      orderRs.next();
      int orderId = orderRs.getInt(1);

      saveDishOrders(con, orderId, orderToSave.getDishOrders());

      updateStock(con, orderToSave);

      con.commit();

      return findOrderById(orderId);

    } catch (SQLException e) {
      rollbackQuietly(con);
      throw new RuntimeException("An error occurred while trying to save order ", e);
    } catch (RuntimeException e) {
      rollbackQuietly(con);
      throw new RuntimeException("Failed to save order ", e);
    } finally {
      restoreAutoCommit(con);
      dbConnection.attemptCloseDBConnection(orderRs, orderStmt, con);
    }
  }

  @Override
  public Order findOrderByReference(String reference) {
    if (reference == null || reference.isBlank()) {
      throw new IllegalArgumentException("reference cannot be null or empty");
    }

    String orderSql =
        """
        select o.id as o_id, o.reference as o_reference,
               o.creation_datetime as o_creation_datetime,
               o.id_table, o.installation_datetime, o.departure_datetime
        from "order" o where reference = ?
        """;

    Connection con = null;
    PreparedStatement orderStmt = null;
    ResultSet orderRs = null;

    try {
      con = dbConnection.getDBConnection();
      orderStmt = con.prepareStatement(orderSql);
      orderStmt.setString(1, reference);
      orderRs = orderStmt.executeQuery();
      if (!orderRs.next()) {
        return null;
      }
      return mapOrderFromResultSet(orderRs);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find order by reference ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(orderRs, orderStmt, con);
    }
  }

  @Override
  public Order findOrderById(Integer orderId) {
    if (orderId == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    String findOrderSql =
        """
        select o.id as o_id, o.reference as o_reference,
               o.creation_datetime as o_creation_datetime,
               o.id_table, o.installation_datetime, o.departure_datetime
        from "order" o where o.id = ?
        """;

    Connection con = null;
    PreparedStatement findOrderStmt = null;
    ResultSet findOrderRs = null;

    try {
      con = dbConnection.getDBConnection();
      findOrderStmt = con.prepareStatement(findOrderSql);
      findOrderStmt.setInt(1, orderId);
      findOrderRs = findOrderStmt.executeQuery();
      if (!findOrderRs.next()) {
        throw new RuntimeException("Order with id " + orderId + " not found");
      }
      return mapOrderFromResultSet(findOrderRs);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find order ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findOrderRs, findOrderStmt, con);
    }
  }

  // DishOrder methods

  @Override
  public List<DishOrder> findDishOrdersByOrderId(Integer orderId) {
    if (orderId == null) {
      throw new IllegalArgumentException("orderId cannot be null");
    }
    String findDishOrdersSql =
        """
        select dor.id as dor_id,
               dor.quantity as dor_quantity,
               dor.id_dish from dish_order dor
                           where dor.id_order = ?
        """;

    Connection con = null;
    PreparedStatement findDishOrdersStmt = null;
    ResultSet findDishOrdersRs = null;

    try {
      con = dbConnection.getDBConnection();
      findDishOrdersStmt = con.prepareStatement(findDishOrdersSql);
      findDishOrdersStmt.setInt(1, orderId);
      findDishOrdersRs = findDishOrdersStmt.executeQuery();
      List<DishOrder> dishOrders = new ArrayList<>();
      while (findDishOrdersRs.next()) {
        dishOrders.add(mapDishOrdersFromResultSet(findDishOrdersRs));
      }
      return dishOrders;
    } catch (SQLException e) {
      throw new RuntimeException("Failed to fetch dish orders of order ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findDishOrdersRs, findDishOrdersStmt, con);
    }
  }

  private void saveDishOrders(Connection con, int orderId, List<DishOrder> dishOrders) {
    if (orderId <= 0) {
      throw new IllegalArgumentException("orderId is invalid : " + orderId);
    }
    if (dishOrders == null || dishOrders.isEmpty()) {
      return;
    }
    for (DishOrder dishOrder : dishOrders) {
      isValid(dishOrder);
      verifyDishExists(con, dishOrder.getDish().getId());
    }

    String dishOrderSql =
        """
                    insert into dish_order (id, id_order, id_dish, quantity) values (?, ?, ?, ?)
                """;

    PreparedStatement dishOrderStmt = null;

    try {
      dishOrderStmt = con.prepareStatement(dishOrderSql);
      for (DishOrder dishOrder : dishOrders) {
        if (dishOrder.getId() == null) {
          dishOrderStmt.setInt(1, getNextSerialValue(con, "dish_order", "id"));
        } else {
          dishOrderStmt.setInt(1, dishOrder.getId());
        }
        dishOrderStmt.setInt(2, orderId);
        dishOrderStmt.setInt(3, dishOrder.getDish().getId());
        dishOrderStmt.setDouble(4, dishOrder.getQuantity());

        dishOrderStmt.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to save new dish order ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(dishOrderStmt);
    }
  }

  // mappers

  private DishOrder mapDishOrdersFromResultSet(ResultSet dishOrderRs) throws SQLException {
    DishOrder dishOrder = new DishOrder();
    dishOrder.setId(dishOrderRs.getInt("dor_id"));
    dishOrder.setDish(findDishById(dishOrderRs.getInt("id_dish")));
    dishOrder.setQuantity(dishOrderRs.getInt("dor_quantity"));
    return dishOrder;
  }

  private Order mapOrderFromResultSet(ResultSet orderRs) throws SQLException {
    Order order = new Order();
    order.setId(orderRs.getInt("o_id"));
    order.setReference(orderRs.getString("o_reference"));
    order.setCreationDatetime(orderRs.getTimestamp("o_creation_datetime").toInstant());
    order.setDishOrders(findDishOrdersByOrderId(orderRs.getInt("o_id")));

    // Ajouter TableOrder si id_table existe
    Integer tableId = orderRs.getObject("id_table", Integer.class);
    if (tableId != null) {
      Table table = findById(tableId);
      if (table != null) {
        Timestamp installationTs = orderRs.getTimestamp("installation_datetime");
        Timestamp departureTs = orderRs.getTimestamp("departure_datetime");

        Instant arrival = installationTs != null ? installationTs.toInstant() : null;
        Instant departure = departureTs != null ? departureTs.toInstant() : null;

        TableOrder tableOrder = new TableOrder(table, arrival, departure);
        order.setTableOrder(tableOrder);
      }
    }

    return order;
  }

  private Ingredient mapIngredientFromResultSet(ResultSet ingRs) throws SQLException {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(ingRs.getInt("i_id"));
    ingredient.setName(ingRs.getString("i_name"));
    ingredient.setPrice(ingRs.getDouble("i_price"));
    ingredient.setCategory(CategoryEnum.valueOf(ingRs.getString("i_category")));
    ingredient.setStockMovementList(findStockMovementsByIngredientId(ingredient.getId()));
    return ingredient;
  }

  private Table mapTableFromResultSet(ResultSet resultSet) throws SQLException {
    Integer id = resultSet.getObject("t_id") == null ? null : resultSet.getInt("t_id");
    Integer number = resultSet.getObject("t_number") == null ? null : resultSet.getInt("t_number");

    if (id == null || number == null) {
      return null;
    }

    return new Table(id, number, null); // we dont change orders here
  }

  private DishIngredient mapDishIngredientFromResultSet(ResultSet dishIngRs) throws SQLException {
    DishIngredient dishIngredient = new DishIngredient();
    dishIngredient.setId(dishIngRs.getInt("di_id"));
    dishIngredient.setQuantityRequired(dishIngRs.getDouble("di_quantity_required"));
    dishIngredient.setUnit(UnitType.valueOf(dishIngRs.getString("di_unit")));
    dishIngredient.setIngredient(findIngredientById(dishIngRs.getInt("id_ingredient")));
    // no need to setDish in ingredient since already set when Dish.setDishIngredients, and we only
    // call this
    // once
    // for that specific scenario
    // edit: there is one that need it (findDishIngredientById) which manually set the dish
    return dishIngredient;
  }

  private Dish mapDishFromResultSet(ResultSet dishRs) throws SQLException {
    Dish dish = new Dish();
    dish.setId(dishRs.getInt("d_id"));
    dish.setName(dishRs.getString("d_name"));
    dish.setDishType(DishTypeEnum.valueOf(dishRs.getString("dish_type")));
    // establish the dish inside the DishIngredient
    dish.setDishIngredients(findDishIngredientsByDishId(dishRs.getInt("d_id")));
    if (hasColumn(dishRs, "d_price")) {
      if (dishRs.getObject("d_price") != null) {
        dish.setPrice(dishRs.getDouble("d_price"));
      }
    }
    return dish;
  }

  private StockMovement mapStockMovementFromResultSet(ResultSet stockMovementRs)
      throws SQLException {
    StockValue stockValue = new StockValue();
    stockValue.setQuantity(stockMovementRs.getDouble("st_quantity"));
    stockValue.setUnit(UnitType.valueOf(stockMovementRs.getString("st_unit")));
    StockMovement stockMovement = new StockMovement();
    stockMovement.setValue(stockValue);
    stockMovement.setId(stockMovementRs.getInt("st_id"));
    stockMovement.setType(MovementTypeEnum.valueOf(stockMovementRs.getString("st_type")));
    stockMovement.setCreationDatetime(
        stockMovementRs.getTimestamp("st_creation_datetime").toInstant());
    return stockMovement;
  }

  // ingredient detach/attach

  private void detachIngredients(
      Connection conn, Integer dishId, List<DishIngredient> dishIngredients) throws SQLException {

    if (dishIngredients == null || dishIngredients.isEmpty()) {
      String detachIngSql = "delete from dish_ingredient where id_dish = ?";
      PreparedStatement detachIngStmt = null;
      try {
        detachIngStmt = conn.prepareStatement(detachIngSql);
        detachIngStmt.setInt(1, dishId);
        detachIngStmt.executeUpdate();
        return;
      } catch (SQLException e) {
        throw new RuntimeException("Failed to supress relation", e);
      } finally {
        dbConnection.attemptCloseDBConnection(detachIngStmt);
      }
    }

    Set<Integer> newIngredientIds = new HashSet<>();
    for (DishIngredient di : dishIngredients) {
      if (di != null && di.getIngredient() != null && di.getIngredient().getId() != null) {
        newIngredientIds.add(di.getIngredient().getId());
      }
    }

    if (newIngredientIds.isEmpty()) {
      String deleteSql = "delete from dish_ingredient where id_dish = ?";
      try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
        ps.setInt(1, dishId);
        ps.executeUpdate();
      }
      return;
    }
    String baseSql =
        """
                            delete from dish_ingredient
                            where id_dish = ? and id_ingredient not in (%s)
                        """;

    String inClause = newIngredientIds.stream().map(i -> "?").collect(Collectors.joining(","));

    String sql = String.format(baseSql, inClause);
    PreparedStatement ps = null;
    try {
      ps = conn.prepareStatement(sql);
      ps.setInt(1, dishId);
      int index = 2;
      for (Integer ingredientId : newIngredientIds) {
        ps.setInt(index++, ingredientId);
      }
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to detach ingredient from dish ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(ps);
    }
  }

  private void attachIngredients(
      Connection conn, Integer dishId, List<DishIngredient> dishIngredients) throws SQLException {

    if (dishIngredients == null || dishIngredients.isEmpty()) {
      return;
    }

    String attachSql =
        """
                            insert into dish_ingredient (id, id_dish, id_ingredient, quantity_required, unit) values
                            (?,?,?,?,?::unit_type)
                            on conflict (id_dish,id_ingredient) do update
                            set quantity_required = excluded.quantity_required, unit = excluded.unit
                        """;

    PreparedStatement ps = null;

    try {
      ps = conn.prepareStatement(attachSql);

      // we have to verify wether the ingredient are legit in the db before moving further
      for (DishIngredient dishIngredient : dishIngredients) {

        if (dishIngredient == null) {
          throw new IllegalArgumentException("DishIngredient cannot be null");
        }

        if (dishIngredient.getIngredient() == null) {
          throw new IllegalArgumentException("Ingredient cannot be null");
        }

        Integer ingredientId = dishIngredient.getIngredient().getId();

        if (ingredientId == null) {
          throw new IllegalArgumentException("Ingredient id cannot be null");
        }
        verifyIngredientExistence(conn, ingredientId);

        if (dishIngredient.getId() == null) {
          ps.setInt(1, getNextSerialValue(conn, "dish_ingredient", "id"));
        } else {
          ps.setInt(1, dishIngredient.getId());
        }

        ps.setInt(2, dishId);
        ps.setInt(3, ingredientId);
        ps.setDouble(4, dishIngredient.getQuantityRequired());
        ps.setString(5, dishIngredient.getUnit().name());
        ps.executeUpdate();
      }

    } catch (SQLException e) {
      throw new RuntimeException("Failed to attach ingredient to dish ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(ps);
    }
  }

  // serial id utils

  private int getNextSerialValue(Connection con, String tableName, String columnName) {
    String sequenceName = getSerialSequenceName(con, tableName, columnName);
    String getNextValueSql = String.format("select nextval('%s')", sequenceName);
    updateSequence(con, columnName, sequenceName, tableName);
    Statement getNextValueStmt = null;
    ResultSet getNextValueRs = null;
    try {
      getNextValueStmt = con.createStatement();
      getNextValueRs = getNextValueStmt.executeQuery(getNextValueSql);
      getNextValueRs.next();
      return getNextValueRs.getInt(1);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get next value ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(getNextValueRs, getNextValueStmt);
    }
  }

  private String getSerialSequenceName(Connection con, String tableName, String columnName) {
    String getSeqSql =
        String.format("SELECT pg_get_serial_sequence('%s', '%s')", tableName, columnName);
    Statement getSeqStmt = null;
    ResultSet getSeqRs = null;
    try {
      getSeqStmt = con.createStatement();
      getSeqRs = getSeqStmt.executeQuery(getSeqSql);
      getSeqRs.next();
      return getSeqRs.getString(1);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to get sequence name", e);
    } finally {
      dbConnection.attemptCloseDBConnection(getSeqRs, getSeqStmt);
    }
  }

  private void updateSequence(
      Connection con, String columnName, String sequenceName, String tableName) {
    String updateSeqSql =
        String.format(
            "select setval('%s', (select coalesce(max(%s),0) from %s))",
            sequenceName, columnName, tableName);
    Statement updateSeqStmt = null;
    try {
      updateSeqStmt = con.createStatement();
      updateSeqStmt.executeQuery(updateSeqSql);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update sequence ", e);
    } finally {
      dbConnection.attemptCloseDBConnection(updateSeqStmt);
    }
  }

  // helper methods

  private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
    ResultSetMetaData meta = rs.getMetaData();
    int columnCount = meta.getColumnCount();

    for (int i = 1; i <= columnCount; i++) {
      if (columnName.equalsIgnoreCase(meta.getColumnLabel(i))) {
        return true;
      }
    }
    return false;
  }

  private void isValid(Ingredient newIngredient) {
    if (newIngredient.getName() == null || newIngredient.getName().isBlank()) {
      throw new IllegalArgumentException("Ingredient name cannot be null or empty");
    }
    if (newIngredient.getCategory() == null) {
      throw new IllegalArgumentException("Ingredient category cannot be null");
    }
    if (newIngredient.getPrice() == null) {
      throw new IllegalArgumentException("Ingredient price cannot be null");
    } else if (newIngredient.getPrice() <= 0) {
      throw new IllegalArgumentException("Ingredient price cannot be negative");
    }
  }

  private void isValid(Dish newDish) {
    if (newDish.getName() == null || newDish.getName().isBlank()) {
      throw new IllegalArgumentException("Dish name cannot be null or empty");
    }
    if (newDish.getDishType() == null) {
      throw new IllegalArgumentException("Dish type cannot be null");
    }
  }

  private void isValid(DishIngredient newDishIngredient) {
    if (newDishIngredient.getDish() == null) {
      throw new IllegalArgumentException("Dish cannot be null");
    }
    if (newDishIngredient.getIngredient() == null) {
      throw new IllegalArgumentException("Ingredient cannot be null");
    }
    if (newDishIngredient.getQuantityRequired() == null) {
      throw new IllegalArgumentException("Quantity required cannot be null or negative");
    } else if (newDishIngredient.getQuantityRequired() <= 0) {
      throw new IllegalArgumentException("Quantity required cannot be negative");
    }
    if (newDishIngredient.getUnit() == null) {
      throw new IllegalArgumentException("Unit cannot be null");
    }
  }

  private void isValid(TableOrder tableOrder) {
    if (tableOrder == null) {
      throw new IllegalArgumentException("TableOrder cannot be null");
    }
    if (tableOrder.getTable() == null) {
      throw new IllegalArgumentException("TableOrder table cannot be null");
    }
    if (tableOrder.getTable().getId() == null) {
      throw new IllegalArgumentException("Table id cannot be null");
    }
    // Vérifier que la table existe
    Table table = findById(tableOrder.getTable().getId());
    if (table == null) {
      throw new IllegalArgumentException(
          "Table with id " + tableOrder.getTable().getId() + " does not exist");
    }
    if (tableOrder.getArrivalDatetime() == null) {
      throw new IllegalArgumentException("Arrival datetime cannot be null");
    }
    // Le departureDatetime peut être null (client encore à table)
  }

  private void isValid(Order order) {
    if (order.getReference() == null || order.getReference().isBlank()) {
      throw new IllegalArgumentException("Order reference cannot be null or empty");
    }
    if (order.getDishOrders() == null || order.getDishOrders().isEmpty()) {
      throw new IllegalArgumentException("Order dishes cannot be null or empty");
    }
    if (order.getTableOrder() != null) {
      isValid(order.getTableOrder());
    }
    for (DishOrder dishOrder : order.getDishOrders()) {
      if (dishOrder.getQuantity() == null) {
        throw new IllegalArgumentException("Dish quantity must be positive");
      } else if (dishOrder.getQuantity() <= 0) {
        throw new IllegalArgumentException("Dish quantity must be positive");
      }
      if (dishOrder.getDish() == null) {
        throw new IllegalArgumentException("Dish cannot be null");
      }
    }
  }

  private void isValid(DishOrder dishOrder) {
    if (dishOrder.getDish() == null || dishOrder.getDish().getId() == null) {
      throw new IllegalArgumentException(
          "Dish order Dish is null or has no id : " + dishOrder.getDish());
    }
    if (dishOrder.getQuantity() == null) {
      throw new IllegalArgumentException("Dish order quantity must be positive");
    } else if (dishOrder.getQuantity() <= 0) {
      throw new IllegalArgumentException("Dish order quantity must be positive");
    }
  }

  private void isValid(StockMovement stockMovement) {
    if (stockMovement.getValue().getQuantity() == null) {
      throw new IllegalArgumentException("Stock movement quantity must be positive");
    } else if (stockMovement.getValue().getQuantity() <= 0) {
      throw new IllegalArgumentException("Stock movement quantity must be positive");
    }
    if (stockMovement.getValue().getUnit() == null) {
      throw new IllegalArgumentException("Stock movement unit cannot be null");
    }
    if (stockMovement.getType() == null) {
      throw new IllegalArgumentException("Stock movement type cannot be null");
    }
  }

  private String getFindIngSql(String ingredientName, CategoryEnum category, String dishName) {
    StringBuilder sqlBuilder =
        new StringBuilder(
            """
                                    select distinct i.id as i_id, i.name as i_name, i.price as i_price, i.category as i_category
                                    from ingredient i
                                    """);

    boolean hasWhere = false;

    if (ingredientName != null && !ingredientName.isBlank()) {
      sqlBuilder.append(" where ");
      hasWhere = true;
      sqlBuilder.append("i.name ilike ?");
    }

    if (category != null) {
      if (hasWhere) {
        sqlBuilder.append(" and ");
      } else {
        sqlBuilder.append(" where ");
        hasWhere = true;
      }
      sqlBuilder.append("i.category = ?::category");
    }

    if (dishName != null && !dishName.isBlank()) {
      if (hasWhere) {
        sqlBuilder.append(" and ");
      } else {
        sqlBuilder.append(" where ");
      }
      sqlBuilder.append(
          "exists (select 1 from dish_ingredient di join dish d on di.id_dish = d.id where di.id_ingredient = i.id and d.name ilike ?)");
    }

    sqlBuilder.append(" order by i.id limit ? offset ?");

    return sqlBuilder.toString();
  }

  private void rollbackQuietly(Connection con) {
    try {
      if (con != null && !con.isClosed()) {
        con.rollback();
      }
    } catch (SQLException e) {
      System.err.println("Warning: Rollback failed: " + e.getMessage());
    }
  }

  private void restoreAutoCommit(Connection con) {
    try {
      if (con != null && !con.isClosed()) {
        con.setAutoCommit(true);
      }
    } catch (SQLException ex) {
      System.out.println("Failed to set autocommit to true");
    }
  }

  // table methods
  @Override
  public Table findById(Integer id) {
    String sql =
        """
    select t.id as t_id, t.number as t_number from "table" t where t.id = ?
    """;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      statement = con.prepareStatement(sql);
      statement.setInt(1, id);

      rs = statement.executeQuery();
      if (!rs.next()) {
        throw new RuntimeException("Table not found");
      }
      return mapTableFromResultSet(rs);
    } catch (SQLException e) {
      throw new RuntimeException("Error finding table by id: " + id, e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, statement, con);
    }
  }

  @Override
  public Table findByNumber(Integer number) {
    String sql =
        """
    select t.id as t_id, t.number as t_number from "table" t where t.number = ?
    """;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      statement = con.prepareStatement(sql);
      statement.setInt(1, number);
      rs = statement.executeQuery();
      if (!rs.next()) {
        throw new RuntimeException("Table not found");
      }
      return mapTableFromResultSet(rs);
    } catch (SQLException e) {
      throw new RuntimeException("Error finding table by number: " + number, e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, statement, con);
    }
  }

  @Override
  public List<Table> findAll() {
    String sql =
        """
    select t.id as t_id, t.number as t_number from "table" t order by t.number
    """;
    List<Table> tables = new ArrayList<>();

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = dbConnection.getDBConnection();
      statement = con.prepareStatement(sql);
      rs = statement.executeQuery();
      while (rs.next()) {
        tables.add(mapTableFromResultSet(rs));
      }
      return tables;
    } catch (SQLException e) {
      throw new RuntimeException("Error finding all tables", e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, statement, con);
    }
  }

  @Override
  public List<Integer> findAvailableTableNumbersAt(Instant instant) {
    String sql =
        """
            select t.number
            from "table" t
            where not exists (
                select 1 from "order" o
                where o.id_table = t.id
                and o.installation_datetime <= ?
                and (o.departure_datetime is null or o.departure_datetime > ?)
            )
            order by t.number
            """;

    List<Integer> availableTableNumbers = new ArrayList<>();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = dbConnection.getDBConnection();
      statement = con.prepareStatement(sql);
      Timestamp timestamp = Timestamp.from(instant);
      statement.setTimestamp(1, timestamp);
      statement.setTimestamp(2, timestamp);

      rs = statement.executeQuery();
      while (rs.next()) {
        availableTableNumbers.add(rs.getInt("number"));
      }
      return availableTableNumbers;
    } catch (SQLException e) {
      throw new RuntimeException("Error finding available table numbers at " + instant, e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, statement, con);
    }
  }

  @Override
  public List<Table> findAvailableTablesAt(Instant instant) {
    String sql =
        """
            select t.id as t_id, t.number as t_number
            from "table" t
            where not exists (
                select 1 from "order" o
                where o.id_table = t.id
                and o.installation_datetime <= ?
                and (o.departure_datetime is null or o.departure_datetime > ?)
            )
            order by t.number
            """;

    List<Table> availableTables = new ArrayList<>();
    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;
    try {
      con = dbConnection.getDBConnection();
      statement = con.prepareStatement(sql);
      Timestamp timestamp = Timestamp.from(instant);
      statement.setTimestamp(1, timestamp);
      statement.setTimestamp(2, timestamp);
      rs = statement.executeQuery();
      while (rs.next()) {
        availableTables.add(mapTableFromResultSet(rs));
      }
      return availableTables;
    } catch (SQLException e) {
      throw new RuntimeException("Error finding available tables at " + instant, e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, statement, con);
    }
  }

  private void checkTableAvailability(Table table, Instant arrivalTime) {

    if (!isTableAvailableAt(table, arrivalTime)) {
      List<Integer> availableTableNumbers = findAvailableTableNumbersAt(arrivalTime);

      if (availableTableNumbers.isEmpty()) {
        throw new RuntimeException(
            "Table "
                + table.getNumber()
                + " is not available at "
                + arrivalTime
                + ". No tables are currently available.");
      } else {
        String tablesList =
            String.join(", ", availableTableNumbers.stream().map(String::valueOf).toList());
        throw new RuntimeException(
            "Table "
                + table.getNumber()
                + " is not available at "
                + arrivalTime
                + ". Available tables: "
                + tablesList
                + " available number : "
                + availableTableNumbers);
      }
    }
  }

  private boolean isTableAvailableAt(Table table, Instant instant) {
    String sql =
        """
        SELECT COUNT(*) = 0 as is_available
        FROM "order" o
        WHERE o.id_table = ?
        AND o.installation_datetime <= ?
        AND (o.departure_datetime IS NULL OR o.departure_datetime > ?)
        """;

    Connection con = null;
    PreparedStatement statement = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      statement = con.prepareStatement(sql);
      Timestamp timestamp = Timestamp.from(instant);
      statement.setInt(1, table.getId());
      statement.setTimestamp(2, timestamp);
      statement.setTimestamp(3, timestamp);

      rs = statement.executeQuery();
      if (rs.next()) {
        return rs.getBoolean("is_available");
      }
      return false;
    } catch (SQLException e) {
      throw new RuntimeException("Error checking table availability", e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, statement, con);
    }
  }
}
