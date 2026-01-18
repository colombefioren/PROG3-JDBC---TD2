package com.jdbctd2.repository;

import com.jdbctd2.db.DBConnection;
import com.jdbctd2.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataRetriever implements IngredientRepository, DishRepository {

  private final DBConnection dbConnection;

  public DataRetriever() {
    this.dbConnection = new DBConnection();
  }

  public void initializeDB() {
    String clearTablesSql =
        """
                truncate table Ingredient, Dish, dish_ingredient restart identity cascade
                """;

    String insertDishSql =
        """
                 INSERT INTO Dish (name, dish_type, selling_price) VALUES
                            ('Salade fraîche', 'START',3500.00),
                            ('Poulet grillé', 'START',12000.00),
                            ('Riz aux légumes', 'MAIN',null),
                            ('Gâteau au chocolat', 'DESSERT',8000.00),
                            ('Salade de fruits', 'DESSERT',null)
                """;

    String insertIngredientSql =
        """
                insert into Ingredient (name, price, category) values
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

    String dishSequenceSql =
"""
SELECT setval('dish_id_seq', (SELECT MAX(id) FROM Dish));
""";

    String ingSequenceSql =
"""
SELECT setval('ingredient_id_seq', (SELECT MAX(id) FROM Ingredient));
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
      stmt.executeQuery(dishSequenceSql);
      stmt.executeQuery(ingSequenceSql);
      stmt.executeUpdate(dishIngSequenceSql);
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
                            from Dish d
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

      Dish dish = new Dish();
      dish.setId(dishRs.getInt("dish_id"));
      dish.setName(dishRs.getString("dish_name"));
      dish.setDishType(DishTypeEnum.valueOf(dishRs.getString("dish_type")));
      dish.setSellingPrice(
          dishRs.getObject("dish_price") == null ? null : dishRs.getDouble("dish_price"));
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
                            INSERT INTO dish (id, price, name, dish_type)
                            VALUES (?, ?, ?, ?::dish_type)
                            ON CONFLICT (id) DO UPDATE
                            SET name = EXCLUDED.name,
                                dish_type = EXCLUDED.dish_type,
                                price = EXCLUDED.price
                            RETURNING id
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
      if (dishToSave.getPrice() != null) {
        upsertDishStmt.setDouble(2, dishToSave.getPrice());
      } else {
        upsertDishStmt.setNull(2, Types.DOUBLE);
      }
      upsertDishStmt.setString(3, dishToSave.getName());
      upsertDishStmt.setString(4, dishToSave.getDishType().name());
      upsertDishRs = upsertDishStmt.executeQuery();
      if (!upsertDishRs.next()) {
        throw new RuntimeException("Error while saving dish with name " + dishToSave.getName());
      }
      savedDishId = upsertDishRs.getInt(1);

      List<Ingredient> newIngredients = dishToSave.getIngredients();
      detachIngredients(con, savedDishId, newIngredients);
      attachIngredients(con, savedDishId, newIngredients);

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
                    select d.id as dish_id, i.name as ing_name
                    from Dish d
                    join Ingredient i on d.id = i.id_dish
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
                select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category, i.id_dish as id_dish
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
                        select ingredient.id as ing_id, ingredient.name as ing_name, ingredient.price as ing_price, ingredient.category as ing_category, ingredient.id_dish as id_dish
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
                                        INSERT INTO ingredient (id, name, category, price)
                                        VALUES (?, ?, ?::category, ?)
                                        RETURNING id
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

  private List<Ingredient> findIngredientByDishId(Integer idDish) {
    String sql =
        """
        select ingredient.id as ing_id, ingredient.name as ing_name, ingredient.price as ing_price, ingredient.category as ing_category
        from ingredient
        join
        where id_dish = ?
        """;

    Connection con = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    List<Ingredient> ingredients = new ArrayList<>();

    try {
      con = dbConnection.getDBConnection();
      preparedStatement = con.prepareStatement(sql);
      preparedStatement.setInt(1, idDish);
      resultSet = preparedStatement.executeQuery();
      while (resultSet.next()) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(resultSet.getInt("ing_id"));
        ingredient.setName(resultSet.getString("ing_name"));
        ingredient.setPrice(resultSet.getDouble("ing_price"));
        ingredient.setCategory(CategoryEnum.valueOf(resultSet.getString("ing_category")));
        ingredients.add(ingredient);
      }
      return ingredients;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      dbConnection.attemptCloseDBConnection(resultSet, preparedStatement, con);
    }
  }

  private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
      throws SQLException {
    if (ingredients == null || ingredients.isEmpty()) {
      try (PreparedStatement ps =
          conn.prepareStatement(
              """
                                        UPDATE ingredient SET id_dish = NULL WHERE id_dish = ?
                    """)) {
        ps.setInt(1, dishId);
        ps.executeUpdate();
      }
      return;
    }

    String baseSql =
        """
                                    UPDATE ingredient
                                    SET id_dish = NULL
                                    WHERE id_dish = ? AND id NOT IN (%s)
                                """;

    String inClause = ingredients.stream().map(i -> "?").collect(Collectors.joining(","));

    String sql = String.format(baseSql, inClause);

    try (PreparedStatement ps = conn.prepareStatement(sql)) {
      ps.setInt(1, dishId);
      int index = 2;
      for (Ingredient ingredient : ingredients) {
        ps.setInt(index++, ingredient.getId());
      }
      ps.executeUpdate();
    }
  }

  private void attachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
      throws SQLException {
    if (ingredients == null || ingredients.isEmpty()) {
      return;
    }

    String attachSql =
        """
                                    UPDATE ingredient
                                    SET id_dish = ?
                                    WHERE id = ?
                                """;

    try (PreparedStatement ps = conn.prepareStatement(attachSql)) {
      for (Ingredient ingredient : ingredients) {
        ps.setInt(1, dishId);
        ps.setInt(2, ingredient.getId());
        ps.addBatch();
      }
      ps.executeBatch();
    }
  }

  private String getFindIngSql(String ingredientName, CategoryEnum category, String dishName) {
    String findIngSql =
        """
                select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category, i.id_dish, d.name as dish_name
                from Ingredient i
                left join Dish d on i.id_dish = d.id
                """;
    boolean hasWhere = false;
    if (ingredientName != null && !ingredientName.isBlank()) {
      findIngSql += "where i.name ilike ?";
      hasWhere = true;
    }
    if (category != null) {
      if (hasWhere) {
        findIngSql += " and ";
      } else {
        findIngSql += " where ";
      }
      findIngSql += "i.category = ?::category";
      hasWhere = true;
    }
    if (dishName != null && !dishName.isBlank()) {
      if (hasWhere) {
        findIngSql += " and ";
      } else {
        findIngSql += " where ";
      }
      findIngSql += "d.name ilike ?";
    }
    findIngSql += " order by ing_id limit ? offset ?";
    return findIngSql;
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
    if (dish.getPrice() != null && dish.getPrice() < 0) {
      throw new IllegalArgumentException("Dish price cannot be negative");
    }
  }

  private Ingredient mapResultSetToIngredient(ResultSet rs) throws SQLException {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(rs.getInt("ing_id"));
    ingredient.setName(rs.getString("ing_name"));
    ingredient.setPrice(rs.getDouble("ing_price"));
    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("ing_category")));
    if (rs.getInt("id_dish") > 0) {
      ingredient.setDish(findDishById(rs.getInt("id_dish")));
    }
    return ingredient;
  }

  private String getSerialSequenceName(Connection conn, String tableName, String columnName)
      throws SQLException {
    String sql = "SELECT pg_get_serial_sequence(?, ?)";
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

    String nextValSql = "SELECT nextval(?)";
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
            "SELECT setval('%s', (SELECT COALESCE(MAX(%s), 0) FROM %s))",
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
        dishIngredients.add(dishIngredient);
      }

      return dishIngredients;

    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      dbConnection.attemptCloseDBConnection(rs, pstmt, con);
    }
  }
}
