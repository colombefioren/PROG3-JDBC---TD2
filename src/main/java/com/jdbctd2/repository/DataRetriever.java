package com.jdbctd2.repository;

import com.jdbctd2.db.DBConnection;
import com.jdbctd2.model.*;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataRetriever implements IngredientRepository, DishRepository, OrderRepository {

  private final DBConnection dbConnection;

  public DataRetriever() {
    this.dbConnection = new DBConnection();
  }

  public void initializeDB() {
    String clearTablesSql =
        """
                    truncate table ingredient, dish, dish_ingredient, stock_movement restart identity cascade
                    """;

    String insertDishSql =
        """
                     insert into dish (name, dish_type, selling_price) values
                                ('Salade fraîche', 'START',3500.00),
                                ('Poulet grillé', 'START',12000.00),
                                ('Riz aux légumes', 'MAIN',null),
                                ('Gâteau au chocolat', 'DESSERT',8000.00),
                                ('Salade de fruits', 'DESSERT',null)
                    """;

    String insertIngredientSql =
        """
                    insert into ingredient (name, price, category) values
                            ('Laitue', 800.00, 'VEGETABLE'),
                            ('Tomate', 600.00, 'VEGETABLE'),
                            ('Poulet', 4500.00, 'ANIMAL'),
                            ('Chocolat', 3000.00, 'OTHER'),
                            ('Beurre', 2500.00, 'DAIRY')
                    """;

    String insertDishIngSql =
        """
                    insert into dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
                    values (1, 1, 0.20, 'KG'),
                           (1, 2, 0.15, 'KG'),
                           (2, 3, 1.00, 'KG'),
                           (4, 4, 0.30, 'KG'),
                           (4, 5, 0.20, 'KG')
                    """;

    String insertStockMovementSql =
"""
insert into stock_movement (id_ingredient, quantity, unit, creation_datetime, type)
values (1, 5.0, 'KG', '2024-01-05 08:00', 'IN'),
       (1, 0.2, 'KG', '2024-01-06 12:00', 'OUT'),
       (2, 4.0, 'KG', '2024-01-05 08:00', 'IN'),
       (2, 0.15, 'KG', '2024-01-06 12:00', 'OUT'),
       (3, 10.0, 'KG', '2024-01-04 09:00', 'IN'),
       (3, 1.0, 'KG', '2024-01-06 13:00', 'OUT'),
       (4, 3.0, 'KG', '2024-01-05 10:00', 'IN'),
       (4, 0.3, 'KG', '2024-01-06 14:00', 'OUT'),
       (5, 2.5, 'KG', '2024-01-05 10:00', 'IN'),
       (5, 0.2, 'KG', '2024-01-06 14:00', 'OUT')
""";

    String stockMovementSequenceSql =
"""
select setval('stock_movement_id_seq', (select max(id) from stock_movement));

""";

    String dishSequenceSql =
        """
                    select setval('dish_id_seq', (select max(id) from dish));
                    """;

    String ingSequenceSql =
        """
                    select setval('ingredient_id_seq', (select max(id) from ingredient));
                    """;

    String dishIngSequenceSql =
        """
                    select setval('dishingredient_id_seq', (select max(id) from dish_ingredient));
                    """;

    Connection con = null;
    Statement stmt = null;
    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      stmt = con.createStatement();
      stmt.executeUpdate(clearTablesSql);
      stmt.executeUpdate(insertDishSql);
      stmt.executeUpdate(insertIngredientSql);
      stmt.executeUpdate(insertDishIngSql);
      stmt.executeUpdate(insertStockMovementSql);
      stmt.executeQuery(stockMovementSequenceSql);
      stmt.executeQuery(dishSequenceSql);
      stmt.executeQuery(ingSequenceSql);
      stmt.executeQuery(dishIngSequenceSql);
      con.commit();
    } catch (SQLException e) {
      try {
        if (con != null && !con.isClosed()) {
          con.rollback();
        }
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException("Failed to initialize the data in db. " + "Error: " + e);
    } finally {
      try {
        if (con != null && !con.isClosed()) {
          con.setAutoCommit(true);
        }
      } catch (SQLException e) {
        System.err.println("Could not reset auto-commit: " + e);
      }
      dbConnection.attemptCloseDBConnection(stmt, con);
    }
  }

  @Override
  public Dish findDishById(Integer id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Dish id must be positive");
    }
    String dishSql =
        """
                                select d.id as dish_id, d.name as dish_name, d.dish_type, d.selling_price as dish_price
                                from dish d
                                where d.id = ?
                                order by dish_id
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
        throw new RuntimeException("Dish with ID " + id + " not found");
      }

      Dish dish = mapResultSetToDish(dishRs);
      dish.setDishIngredients(findDishIngredientsByDishId(id));
      return dish;
    } catch (SQLException e) {
      throw new RuntimeException("Error while trying to retrieve dish with id " + id + e);
    } finally {
      dbConnection.attemptCloseDBConnection(dishRs, dishStmt, con);
    }
  }

  @Override
  public Dish saveDish(Dish dishToSave) {
    if (dishToSave == null) {
      throw new IllegalArgumentException("Dish to save cannot be null");
    }
    isValid(dishToSave);

    String upsertDishSql =
        """
                                insert into dish (id, name, dish_type,selling_price)
                                values (?, ?, ?::dish_type,?)
                                on conflict (id) do update
                                set name = excluded.name,
                                    dish_type = excluded.dish_type,
                                    selling_price = excluded.selling_price
                                returning id
                            """;

    Connection con = null;
    PreparedStatement upsertDishStmt = null;
    ResultSet upsertDishRs = null;

    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      Integer savedDishId;

      upsertDishStmt = con.prepareStatement(upsertDishSql);
      if (dishToSave.getId() != null) {
        upsertDishStmt.setInt(1, dishToSave.getId());
      } else {
        upsertDishStmt.setInt(1, getNextSerialValue(con, "dish", "id"));
      }
      upsertDishStmt.setString(2, dishToSave.getName());
      upsertDishStmt.setString(3, dishToSave.getDishType().name());

      if (dishToSave.getSellingPrice() != null) {
        upsertDishStmt.setDouble(4, dishToSave.getSellingPrice());
      } else {
        upsertDishStmt.setNull(4, Types.DOUBLE);
      }

      upsertDishRs = upsertDishStmt.executeQuery();
      if (!upsertDishRs.next()) {
        throw new RuntimeException("Error while saving dish with name " + dishToSave.getName());
      }
      savedDishId = upsertDishRs.getInt(1);

      List<DishIngredient> newDishIngredients = dishToSave.getDishIngredients();

      detachDishIngredients(con, savedDishId, newDishIngredients);

      attachDishIngredients(con, savedDishId, newDishIngredients);

      con.commit();
      return findDishById(savedDishId);
    } catch (SQLException e) {
      try {
        if (con != null && !con.isClosed()) {
          con.rollback();
          System.out.println("An error occurred so that transaction was rolled back");
        }
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException("Failed to save dish " + dishToSave.getName() + ". Error : " + e);
    } finally {
      try {
        if (con != null && !con.isClosed()) {
          con.setAutoCommit(true);
        }
      } catch (SQLException e) {
        System.err.println("Could not reset auto-commit: " + e);
      }
      dbConnection.attemptCloseDBConnection(upsertDishRs, upsertDishStmt, con);
    }
  }

  @Override
  public List<Dish> findDishesByIngredientName(String IngredientName) {
    String findIngSql =
        """
                        select distinct d.id as dish_id
                        from dish d
                        join dish_ingredient di on d.id = di.id_dish
                        join ingredient i on di.id_ingredient = i.id
                        where i.name ilike ?
                    """;

    Connection con = null;
    PreparedStatement findIngSmt = null;
    ResultSet findIngRs = null;

    try {
      con = dbConnection.getDBConnection();
      findIngSmt = con.prepareStatement(findIngSql);
      findIngSmt.setString(1, "%" + IngredientName + "%");
      findIngRs = findIngSmt.executeQuery();
      List<Dish> dishes = new ArrayList<>();
      while (findIngRs.next()) {
        dishes.add(findDishById(findIngRs.getInt("dish_id")));
      }
      return dishes;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      dbConnection.attemptCloseDBConnection(findIngRs, findIngSmt, con);
    }
  }

  @Override
  public Ingredient findIngredientByName(String ingredientName) {
    String findIngByNameSql =
        """
                    select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category
                    from ingredient i
                    where lower(i.name) = lower(?)
                    order by ing_id
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
      return mapResultSetToIngredient(findIngByNameRs);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      dbConnection.attemptCloseDBConnection(findIngByNameRs, findIngByNameStmt, con);
    }
  }

  @Override
  public List<Ingredient> findIngredients(int page, int size) {
    if (page <= 0 || size <= 0) {
      throw new IllegalArgumentException("Page and size must be valid values");
    }
    String sql =
        """
                            select ingredient.id as ing_id, ingredient.name as ing_name, ingredient.price as ing_price, ingredient.category as ing_category
                            from ingredient
                            order by ingredient.id
                            limit ? offset ?
                    """;
    int offset = (page - 1) * size;
    Connection con = null;
    PreparedStatement ingredientStmt = null;
    ResultSet ingredientRs = null;

    try {
      con = dbConnection.getDBConnection();
      ingredientStmt = con.prepareStatement(sql);
      ingredientStmt.setInt(1, size);
      ingredientStmt.setInt(2, offset);
      ingredientRs = ingredientStmt.executeQuery();
      List<Ingredient> ingredients = new ArrayList<>();
      while (ingredientRs.next()) {
        ingredients.add(mapResultSetToIngredient(ingredientRs));
      }
      return ingredients;
    } catch (SQLException e) {
      throw new RuntimeException("Error while trying to fetch ingredients", e);
    } finally {
      dbConnection.attemptCloseDBConnection(ingredientRs, ingredientStmt, con);
    }
  }

  @Override
  public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
    if (newIngredients == null || newIngredients.isEmpty()) {
      throw new IllegalArgumentException("Ingredients list cannot be empty");
    }

    for (Ingredient newIngredient : newIngredients) {
      if (newIngredient == null) {
        throw new IllegalArgumentException("Ingredients list cannot contain null values");
      }
      isValid(newIngredient);
    }

    Set<String> ingredientNames = new HashSet<>();
    for (Ingredient newIngredient : newIngredients) {
      if (!ingredientNames.add(newIngredient.getName().toLowerCase())) {
        throw new IllegalArgumentException(
            "Duplicate ingredient in provided list" + newIngredient.getName());
      }
    }

    String insertSql =
        """
                                            insert into ingredient (id, name, category, price)
                                            values (?, ?, ?::category, ?)
                                            returning id
                                        """;

    Connection con = null;
    PreparedStatement insertStmt = null;
    ResultSet generatedKeys = null;

    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      insertStmt = con.prepareStatement(insertSql);
      List<Ingredient> createdIngredients = new ArrayList<>();

      for (Ingredient ingredient : newIngredients) {
        if (ingredient.getId() != null) {
          insertStmt.setInt(1, ingredient.getId());
        } else {
          insertStmt.setInt(1, getNextSerialValue(con, "ingredient", "id"));
        }
        insertStmt.setString(2, ingredient.getName());
        insertStmt.setString(3, ingredient.getCategory().name());
        insertStmt.setDouble(4, ingredient.getPrice());
        generatedKeys = insertStmt.executeQuery();
        if (generatedKeys.next()) {
          int generatedId = generatedKeys.getInt(1);
          ingredient.setId(generatedId);
          createdIngredients.add(ingredient);
        }
      }

      con.commit();
      return createdIngredients;
    } catch (SQLException e) {
      try {
        if (con != null && !con.isClosed()) {
          con.rollback();
          System.out.println("An error occurred so that transaction was rolled back");
        }
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException(
          "Failed to create ingredients so all changes rolled back. " + "Error: " + e);
    } finally {
      try {
        if (con != null && !con.isClosed()) {
          con.setAutoCommit(true);
        }
      } catch (SQLException e) {
        System.err.println("Could not reset auto-commit: " + e);
      }
      dbConnection.attemptCloseDBConnection(generatedKeys, insertStmt, con);
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
      findIngStmt.setInt(paramIndex++, (page - 1) * size);

      findIngRs = findIngStmt.executeQuery();
      List<Ingredient> ingredients = new ArrayList<>();
      while (findIngRs.next()) {
        ingredients.add(mapResultSetToIngredient(findIngRs));
      }
      return ingredients;
    } catch (SQLException e) {
      throw new RuntimeException(e);
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

    String upsertIngSql =
"""
  insert into ingredient (id, name, price, category)
     values (?, ?, ?, ?::category)
     on conflict (id) do update
        set name = excluded.name,
            price = excluded.price,
            category = excluded.category
        returning id
""";

    Connection con = null;
    PreparedStatement upsertIngStmt = null;
    ResultSet upsertIngRs = null;

    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      Integer savedIngredientId;

      upsertIngStmt = con.prepareStatement(upsertIngSql);

      if (toSave.getId() != null) {
        upsertIngStmt.setInt(1, toSave.getId());
      } else {
        upsertIngStmt.setInt(1, getNextSerialValue(con, "ingredient", "id"));
      }
      upsertIngStmt.setString(2, toSave.getName());
      upsertIngStmt.setDouble(3, toSave.getPrice());
      upsertIngStmt.setString(4, toSave.getCategory().name());

      upsertIngRs = upsertIngStmt.executeQuery();

      if (!upsertIngRs.next()) {
        throw new RuntimeException("Error while saving ingredient with name: " + toSave.getName());
      }

      savedIngredientId = upsertIngRs.getInt(1);

      List<StockMovement> stockMovementList = toSave.getStockMovements();
      if (stockMovementList != null && !stockMovementList.isEmpty()) {
        saveStockMovements(con, savedIngredientId, stockMovementList);
      }

      con.commit();

      return findIngredientById(savedIngredientId);
    } catch (SQLException e) {
      try {
        if (con != null && !con.isClosed()) {
          con.rollback();
          System.out.println("An error occurred so that transaction was rolled back");
        }
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException("Failed to save ingredient " + toSave.getName() + ". Error: " + e);
    } finally {
      try {
        if (con != null && !con.isClosed()) {
          con.setAutoCommit(true);
        }
      } catch (SQLException e) {
        System.err.println("Could not reset auto-commit: " + e);
      }
      dbConnection.attemptCloseDBConnection(upsertIngRs, upsertIngStmt, con);
    }
  }

  private void saveStockMovements(
      Connection con, Integer ingredientId, List<StockMovement> stockMovements)
      throws SQLException {
    if (stockMovements == null || stockMovements.isEmpty()) {
      return;
    }

    String insertStockMovementSql =
        """
                insert into stock_movement (id, id_ingredient, quantity, unit, creation_datetime, type)
                values (?, ?, ?, ?::unit_type, ?, ?::movement_type)
                on conflict (id) do nothing
                """;

    PreparedStatement insertStockMovementStmt = null;

    try {
      insertStockMovementStmt = con.prepareStatement(insertStockMovementSql);

      for (StockMovement movement : stockMovements) {
        if (movement == null || movement.getValue() == null) {
          continue;
        }

        if (movement.getId() != null) {
          insertStockMovementStmt.setInt(1, movement.getId());
        } else {
          insertStockMovementStmt.setInt(1, getNextSerialValue(con, "stock_movement", "id"));
        }

        insertStockMovementStmt.setInt(2, ingredientId);
        insertStockMovementStmt.setDouble(3, movement.getValue().getQuantity());
        insertStockMovementStmt.setString(4, movement.getValue().getUnit().name());

        if (movement.getCreationDatetime() != null) {
          insertStockMovementStmt.setTimestamp(5, Timestamp.from(movement.getCreationDatetime()));
        } else {
          insertStockMovementStmt.setTimestamp(5, Timestamp.from(Instant.now()));
        }

        insertStockMovementStmt.setString(6, movement.getType().name());
        insertStockMovementStmt.addBatch();
      }

      insertStockMovementStmt.executeBatch();
    } finally {
      if (insertStockMovementStmt != null) {
        try {
          insertStockMovementStmt.close();
        } catch (SQLException e) {
          System.err.println("Could not close statement: " + e);
        }
      }
    }
  }

  private List<StockMovement> saveStockMovement(
      StockMovement stockMovementToSave, Integer ingredientId) {
    if (stockMovementToSave == null) {
      throw new IllegalArgumentException("Stock movement cannot be null");
    }

    String insertStockMovementSql =
        """
                    insert into stock_movement (id, id_ingredient, quantity, unit, creation_datetime, type)
                    values (?, ?, ?, ?::unit_type, ?, ?::movement_type)
                    on conflict (id) do nothing
                    returning id
                    """;

    Connection con = null;
    PreparedStatement insertStockMovementStmt = null;
    ResultSet insertStockMovementRs = null;

    try {
      insertStockMovementStmt = con.prepareStatement(insertStockMovementSql);

      if (stockMovementToSave.getId() != null) {
        insertStockMovementStmt.setInt(1, stockMovementToSave.getId());
      } else {
        insertStockMovementStmt.setInt(1, getNextSerialValue(con, "stock_movement", "id"));
      }

      insertStockMovementStmt.setInt(2, ingredientId);
      insertStockMovementStmt.setDouble(3, stockMovementToSave.getValue().getQuantity());
      insertStockMovementStmt.setString(4, stockMovementToSave.getValue().getUnit().name());

      if (stockMovementToSave.getCreationDatetime() != null) {
        insertStockMovementStmt.setTimestamp(
            5, Timestamp.from(stockMovementToSave.getCreationDatetime()));
      } else {
        insertStockMovementStmt.setTimestamp(5, Timestamp.from(Instant.now()));
      }

      insertStockMovementStmt.setString(6, stockMovementToSave.getType().name());

      insertStockMovementStmt.executeQuery();

      return findStockMovementsByIngredientId(ingredientId);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (insertStockMovementStmt != null) {
        try {
          insertStockMovementStmt.close();
        } catch (SQLException e) {
          System.err.println("Could not close statement: " + e);
        }
      }
    }
  }

  private Ingredient findIngredientById(Integer id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Ingredient id must be positive");
    }

    String sql =
        """
        select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category
        from ingredient i
        where i.id = ?
        """;

    Connection con = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      stmt = con.prepareStatement(sql);
      stmt.setInt(1, id);
      rs = stmt.executeQuery();

      if (!rs.next()) {
        throw new RuntimeException("Ingredient with ID " + id + " not found");
      }

      return mapResultSetToIngredient(rs);
    } catch (SQLException e) {
      throw new RuntimeException("Error while trying to retrieve ingredient with id " + id, e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, stmt, con);
    }
  }

  private List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId) {
    String sql =
        """
        select sm.id as sm_id, sm.quantity, sm.unit, sm.creation_datetime, sm.type
        from stock_movement sm
        where sm.id_ingredient = ?
        order by sm.creation_datetime
        """;

    Connection con = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      stmt = con.prepareStatement(sql);
      stmt.setInt(1, ingredientId);
      rs = stmt.executeQuery();

      List<StockMovement> movements = new ArrayList<>();
      while (rs.next()) {
        movements.add(mapResultSetToStockMovement(rs));
      }
      return movements;
    } catch (SQLException e) {
      throw new RuntimeException(
          "Error while retrieving stock movements for ingredient " + ingredientId, e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, stmt, con);
    }
  }

  private String getFindIngSql(String ingredientName, CategoryEnum category, String dishName) {
    StringBuilder sqlBuilder =
        new StringBuilder(
            """
                    select distinct i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category
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
        hasWhere = true;
      }
      sqlBuilder.append(
          "exists (select 1 from dish_ingredient di join dish d on di.id_dish = d.id where di.id_ingredient = i.id and d.name ilike ?)");
    }

    sqlBuilder.append(" order by i.id limit ? offset ?");

    return sqlBuilder.toString();
  }

  private void isValid(Ingredient ingredient) {
    if (ingredient.getName() == null || ingredient.getName().isBlank()) {
      throw new IllegalArgumentException("Ingredient cannot be empty");
    }
    if (ingredient.getPrice() == null || ingredient.getPrice() < 0) {
      throw new IllegalArgumentException("Ingredient price cannot be negative");
    }
    if (ingredient.getCategory() == null) {
      throw new IllegalArgumentException("Ingredient category cannot be null");
    }
  }

  private void isValid(Dish dish) {
    if (dish.getName() == null || dish.getName().isBlank()) {
      throw new IllegalArgumentException("Dish cannot be empty");
    }
    if (dish.getDishType() == null) {
      throw new IllegalArgumentException("Dish type cannot be null");
    }
    if (dish.getId() != null) {
      if (dish.getId() <= 0) {
        throw new IllegalArgumentException("Dish id cannot be negative");
      }
    }
    if (dish.getSellingPrice() != null && dish.getSellingPrice() < 0) {
      throw new IllegalArgumentException("Dish price cannot be negative");
    }
  }

  private StockMovement mapResultSetToStockMovement(ResultSet rs) throws SQLException {
    StockMovement movement = new StockMovement();
    movement.setId(rs.getInt("sm_id"));
    movement.setType(MovementTypeEnum.valueOf(rs.getString("type")));

    Timestamp timestamp = rs.getTimestamp("creation_datetime");
    if (timestamp != null) {
      movement.setCreationDatetime(timestamp.toInstant());
    }

    StockValue stockValue = new StockValue();
    stockValue.setQuantity(rs.getDouble("quantity"));
    stockValue.setUnit(UnitEnum.valueOf(rs.getString("unit")));
    movement.setValue(stockValue);

    return movement;
  }

  private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
    Ingredient ingredient = new Ingredient();
    int ingredientId = rs.getInt("ing_id");
    ingredient.setId(ingredientId);
    ingredient.setName(rs.getString("ing_name"));
    ingredient.setPrice(rs.getDouble("ing_price"));
    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("ing_category")));
    ingredient.setStockMovements(findStockMovementsByIngredientId(ingredientId));
    return ingredient;
  }

  private Dish mapResultSetToDish(ResultSet rs) throws SQLException {
    Dish dish = new Dish();
    dish.setId(rs.getInt("dish_id"));
    dish.setName(rs.getString("dish_name"));
    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
    dish.setSellingPrice(rs.getObject("dish_price") == null ? null : rs.getDouble("dish_price"));
    return dish;
  }

  private DishIngredient mapResultSetToDishIngredient(ResultSet rs) throws SQLException {
    DishIngredient dishIngredient = new DishIngredient();
    dishIngredient.setId(rs.getInt("di_id"));
    dishIngredient.setQuantityRequired(rs.getDouble("quantity_required"));
    dishIngredient.setUnit(UnitEnum.valueOf(rs.getString("unit")));

    Ingredient ingredient = new Ingredient();
    ingredient.setId(rs.getInt("ing_id"));
    ingredient.setName(rs.getString("ing_name"));
    ingredient.setPrice(rs.getDouble("ing_price"));
    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("ing_category")));
    dishIngredient.setIngredient(ingredient);

    return dishIngredient;
  }

  private String getSerialSequenceName(Connection conn, String tableName, String columnName)
      throws SQLException {
    String sql = "select pg_get_serial_sequence(?, ?)";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setString(1, tableName);
      ps.setString(2, columnName);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return rs.getString(1);
        }
      }
    }
    return null;
  }

  private int getNextSerialValue(Connection conn, String tableName, String columnName)
      throws SQLException {
    String sequenceName = getSerialSequenceName(conn, tableName, columnName);
    if (sequenceName == null) {
      throw new IllegalArgumentException("Any sequence found for " + tableName + "." + columnName);
    }
    updateSequenceNextValue(conn, tableName, columnName, sequenceName);

    String nextValSql = "select nextval(?)";
    try (PreparedStatement ps = conn.prepareStatement(nextValSql)) {
      ps.setString(1, sequenceName);
      try (ResultSet rs = ps.executeQuery()) {
        rs.next();
        return rs.getInt(1);
      }
    }
  }

  private void updateSequenceNextValue(
      Connection conn, String tableName, String columnName, String sequenceName)
      throws SQLException {
    String setValSql =
        String.format(
            "select setval('%s', (select coalesce(max(%s), 0) from %s))",
            sequenceName, columnName, tableName);
    try (PreparedStatement ps = conn.prepareStatement(setValSql)) {
      ps.executeQuery();
    }
  }

  private List<DishIngredient> findDishIngredientsByDishId(Integer dishId) {
    String sql =
        """
              select di.id as di_id, di.quantity_required, di.unit,
              i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category
              from dish_ingredient di
              join ingredient i on di.id_ingredient = i.id
              where di.id_dish = ?
              order by di_id
            """;

    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      pstmt = con.prepareStatement(sql);
      pstmt.setInt(1, dishId);
      rs = pstmt.executeQuery();

      List<DishIngredient> dishIngredients = new ArrayList<>();
      while (rs.next()) {
        dishIngredients.add(mapResultSetToDishIngredient(rs));
      }

      return dishIngredients;

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, pstmt, con);
    }
  }

  private void detachDishIngredients(
      Connection con, Integer dishId, List<DishIngredient> newDishIngredients) throws SQLException {

    if (newDishIngredients == null || newDishIngredients.isEmpty()) {
      try (PreparedStatement ps =
          con.prepareStatement("delete from dish_ingredient where id_dish = ?")) {
        ps.setInt(1, dishId);
        ps.executeUpdate();
      }
      return;
    }

    Set<Integer> newIngredientIds =
        newDishIngredients.stream()
            .filter(
                di ->
                    di != null && di.getIngredient() != null && di.getIngredient().getId() != null)
            .map(di -> di.getIngredient().getId())
            .collect(Collectors.toSet());

    if (newIngredientIds.isEmpty()) {
      try (PreparedStatement ps =
          con.prepareStatement("delete from dish_ingredient where id_dish = ?")) {
        ps.setInt(1, dishId);
        ps.executeUpdate();
      }
      return;
    }

    String placeholders = newIngredientIds.stream().map(id -> "?").collect(Collectors.joining(","));

    String deleteSql =
        String.format(
            "delete from dish_ingredient where id_dish = ? and id_ingredient not in (%s)",
            placeholders);

    try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
      ps.setInt(1, dishId);
      int index = 2;
      for (Integer ingredientId : newIngredientIds) {
        ps.setInt(index++, ingredientId);
      }
      ps.executeUpdate();
    }
  }

  private void attachDishIngredients(
      Connection conn, Integer dishId, List<DishIngredient> newDishIngredients)
      throws SQLException {

    if (newDishIngredients == null || newDishIngredients.isEmpty()) {
      return;
    }

    String upsertSql =
        """
                insert into dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
                values (?, ?, ?, ?::unit_type)
                on conflict (id_dish, id_ingredient) do update
                set quantity_required = excluded.quantity_required,
                    unit = excluded.unit
                """;

    try (PreparedStatement ps = conn.prepareStatement(upsertSql)) {
      for (DishIngredient dishIngredient : newDishIngredients) {
        if (dishIngredient == null || dishIngredient.getIngredient() == null) {
          continue;
        }

        Ingredient ingredient = dishIngredient.getIngredient();

        if (ingredient.getId() == null) {
          ingredient = saveOrGetIngredient(conn, ingredient);
        }

        ps.setInt(1, dishId);
        ps.setInt(2, ingredient.getId());
        ps.setDouble(3, dishIngredient.getQuantityRequired());
        ps.setString(4, dishIngredient.getUnit().name());
        ps.addBatch();
      }
      ps.executeBatch();
    }
  }

  private Ingredient saveOrGetIngredient(Connection conn, Ingredient ingredient)
      throws SQLException {
    Ingredient existing = findIngredientByNameInTransaction(conn, ingredient.getName());

    if (existing != null) {
      return existing;
    }

    isValid(ingredient);

    String insertSql =
        """
                insert into ingredient (name, price, category)
                values (?, ?, ?::category)
                returning id
                """;

    try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
      stmt.setString(1, ingredient.getName());
      stmt.setDouble(2, ingredient.getPrice());
      stmt.setString(3, ingredient.getCategory().name());

      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          ingredient.setId(rs.getInt(1));
          return ingredient;
        } else {
          throw new SQLException("Failed to save ingredient");
        }
      }
    }
  }

  private Ingredient findIngredientByNameInTransaction(Connection conn, String ingredientName)
      throws SQLException {
    String sql =
        """
                select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category
                from ingredient i
                where lower(i.name) = lower(?)
                """;

    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, ingredientName);
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToIngredient(rs);
        }
      }
    }
    return null;
  }

  @Override
  public Order saveOrder(Order orderToSave) {
    if (orderToSave == null) {
      throw new IllegalArgumentException("Order to save cannot be null");
    }

    if (!isStockEnough(orderToSave)) {
      throw new IllegalArgumentException("Not enough stock");
    }

    String saveOrderSql =
"""
    insert into "order" (reference, creation_datetime)
    values (?,?)
    returning id
""";

    String saveDishOrderSql =
"""
    insert into dish_order (id_order, id_dish, quantity)
    values (?,?,?)
    returning id
""";

    Connection con = null;
    PreparedStatement saveOrderStmt = null;
    PreparedStatement saveDishOrderStmt = null;
    ResultSet orderRs = null;
    List<Integer> dishOrderIds = new ArrayList<>();

    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      saveOrderStmt = con.prepareStatement(saveOrderSql);
      saveDishOrderStmt = con.prepareStatement(saveDishOrderSql);

      saveOrderStmt.setString(1, orderToSave.getReference());
      saveOrderStmt.setTimestamp(2, Timestamp.from(orderToSave.getCreationDatetime()));
      orderRs = saveOrderStmt.executeQuery();

      if (!orderRs.next()) {
        throw new RuntimeException("Error while saving order");
      }
      int orderId = orderRs.getInt(1);

      for (DishOrder dishOrder : orderToSave.getDishOrders()) {
        saveDishOrderStmt.setInt(1, orderId);
        saveDishOrderStmt.setInt(2, dishOrder.getDish().getId());
        saveDishOrderStmt.setInt(3, dishOrder.getQuantity());
        saveOrderStmt.addBatch();
      }
      saveOrderStmt.executeBatch();
      updateStock(con, orderToSave);
      con.commit();
      return findOrderById(orderId);
    } catch (SQLException e) {
      try {
        if (con != null && !con.isClosed()) {
          con.rollback();
        }
      } catch (SQLException ex) {
        throw new RuntimeException(ex);
      }
      throw new RuntimeException("Failed to save order: " + e.getMessage(), e);
    } finally {
      try {
        if (con != null && !con.isClosed()) {
          con.setAutoCommit(true);
        }
      } catch (SQLException e) {
        System.err.println("Could not reset auto-commit: " + e);
      }
      dbConnection.attemptCloseDBConnection(orderRs, saveOrderStmt, saveDishOrderStmt, con);
    }
  }

  public Order findOrderById(Integer orderId) {
    if (orderId == null) {
      throw new IllegalArgumentException("Order id cannot be null");
    }

    String findOrderSql =
"""
     select o.id as o_id, o.reference as o_reference, o.creation_datetime as o_datetime from "order" o where o.id = ?
""";

    Connection con = null;
    PreparedStatement findOrderStmt = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      findOrderStmt = con.prepareStatement(findOrderSql);
      findOrderStmt.setInt(1, orderId);
      rs = findOrderStmt.executeQuery();
      if (!rs.next()) {
        throw new RuntimeException("Order with id " + orderId + " not found");
      }

      Order order = mapResultSetToOrder(rs);
      order.setDishOrders(findDishOrdersByOrderId(orderId));

      return order;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, findOrderStmt, con);
    }
  }

  private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
    Order order = new Order();
    order.setId(rs.getInt("o_id"));
    order.setReference(rs.getString("o_reference"));

    Timestamp timestamp = rs.getTimestamp("o_datetime");
    if (timestamp != null) {
      order.setCreationDatetime(timestamp.toInstant());
    }

    return order;
  }

  public List<DishOrder> findDishOrdersByOrderId(Integer orderId) {
    String findDishOrdersSql =
"""
select disho.id as do_id, disho.quantity as di_quantity, disho.id_dish from dish_order disho where do.id_order = ?
""";

    Connection con = null;
    PreparedStatement findDishOrdersStmt = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      findDishOrdersStmt = con.prepareStatement(findDishOrdersSql);
      findDishOrdersStmt.setInt(1, orderId);
      rs = findDishOrdersStmt.executeQuery();
      List<DishOrder> dishOrders = new ArrayList<>();
      while (rs.next()) {
        dishOrders.add(mapResultSetToDishOrder(rs));
      }
      return dishOrders;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private DishOrder mapResultSetToDishOrder(ResultSet rs) throws SQLException {
    DishOrder dishOrder = new DishOrder();
    dishOrder.setId(rs.getInt("do_id"));
    dishOrder.setDish(findDishById(rs.getInt("id_dish")));

    dishOrder.setQuantity(rs.getInt("do_quantity"));

    return dishOrder;
  }

  @Override
  public Order findOrderByReference(String reference) {
    if (reference == null || reference.isBlank()) {
      throw new IllegalArgumentException("Order reference cannot be null or blank");
    }

    String sql =
        """
        select o.id as o_id, o.reference as o_reference,
               o.creation_datetime as o_datetime
        from "order" o
        where o.reference = ?
        """;

    Connection con = null;
    PreparedStatement stmt = null;
    ResultSet rs = null;

    try {
      con = dbConnection.getDBConnection();
      stmt = con.prepareStatement(sql);
      stmt.setString(1, reference);
      rs = stmt.executeQuery();

      if (!rs.next()) {
        throw new RuntimeException("Order with reference '" + reference + "' not found");
      }

      Order order = mapResultSetToOrder(rs);
      order.setDishOrders(findDishOrdersByOrderId(order.getId()));
      return order;

    } catch (SQLException e) {
      throw new RuntimeException(
          "Error while trying to retrieve order with reference " + reference, e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, stmt, con);
    }
  }

  public boolean isStockEnough(Order order) {

    for (DishOrder dishOrder : order.getDishOrders()) {
      Dish dish = findDishById(dishOrder.getDish().getId());
      for (DishIngredient dishIngredient : dish.getDishIngredients()) {
        if (dishIngredient.getQuantityRequired() * dishOrder.getQuantity()
            > dishIngredient.getIngredient().getStockValueAt(Instant.now()).getQuantity()) {
          return false;
        }
      }
    }
    return true;
  }

  private void updateStock(Connection con, Order orderToSave) {
    String insertStockMovementSql =
        """
        insert into stock_movement (id_ingredient, quantity, unit, creation_datetime, type)
        values (?, ?, ?::unit_type, ?, 'OUT')
        """;

    try (PreparedStatement ps = con.prepareStatement(insertStockMovementSql)) {
      for (DishOrder dishOrder : orderToSave.getDishOrders()) {
        Dish dish = dishOrder.getDish();

        for (DishIngredient dishIngredient : dish.getDishIngredients()) {
          double quantityToDeduct = dishIngredient.getQuantityRequired() * dishOrder.getQuantity();

          ps.setInt(1, dishIngredient.getIngredient().getId());
          ps.setDouble(2, quantityToDeduct);
          ps.setString(3, dishIngredient.getUnit().name());
          ps.setTimestamp(4, Timestamp.from(Instant.now()));
          ps.addBatch();
        }
      }
      ps.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
