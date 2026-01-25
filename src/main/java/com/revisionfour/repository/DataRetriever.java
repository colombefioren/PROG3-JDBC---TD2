package com.revisionfour.repository;

import com.revisionfour.db.DBConnection;
import com.revisionfour.model.CategoryEnum;
import com.revisionfour.model.Dish;
import com.revisionfour.model.DishTypeEnum;
import com.revisionfour.model.Ingredient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DataRetriever implements IngredientRepository, DishRepository {
  private final DBConnection dbConnection;

  public DataRetriever() {
    this.dbConnection = new DBConnection();
  }

  public void initializeDB() {
    String eraseDataSql =
"""
        truncate Ingredient,Dish;
""";
    String ingredientDataSql =
"""
        insert into Ingredient (id, name, price, category, id_dish) values
            (1, 'Laitue', 800.00, 'VEGETABLE', 1),
            (2, 'Tomate', 600.00, 'VEGETABLE', 1),
            (3, 'Poulet', 4500.00, 'ANIMAL', 2),
            (4, 'Chocolat', 3000.00, 'OTHER', 4),
            (5, 'Beurre', 2500.00, 'DAIRY', 4);
""";

    String dishDataSql =
"""
      insert into Dish (id, name, dish_type) values
            (1, 'Salade fraîche', 'START'),
            (2, 'Poulet grillé', 'MAIN'),
            (3, 'Riz aux légumes', 'MAIN'),
            (4, 'Gâteau au chocolat', 'DESSERT'),
            (5, 'Salade de fruits', 'DESSERT');
""";

    String dishSqSql =
"""
        select setval('dish_id_seq', (select max(id) from Dish));
""";

    String ingredientSqSql =
        """
        select setval('ingredient_id_seq', (select max(id) from Ingredient));
    """;

    Connection con = null;
    Statement stmt = null;

    try {
      con = dbConnection.getDBConnection();
      stmt = con.createStatement();
      stmt.executeUpdate(eraseDataSql);
      stmt.executeUpdate(dishDataSql);
      stmt.executeUpdate(ingredientDataSql);
      stmt.executeQuery(dishSqSql);
      stmt.executeQuery(ingredientSqSql);
    } catch (SQLException e) {
      throw new RuntimeException("Error while initializing db", e);
    } finally {
      dbConnection.attemptCloseDBConnection(stmt, con);
    }
  }

  @Override
  public Dish findDishById(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    String dishSql =
"""
    select d.id as d_id, d.name as d_name, d.dish_type from dish where d.id = ?
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
      Dish dish = mapDishFromResultSet(dishRs);
      dish.setIngredients(findIngredientsByDishId(id));

      return dish;
    } catch (SQLException e) {
      throw new RuntimeException("Error while trying to fetch dish", e);
    } finally {
      dbConnection.attemptCloseDBConnection(dishRs, dishStmt, con);
    }
  }

  private List<Ingredient> findIngredientsByDishId(Integer id) {
    if (id == null) {
      throw new IllegalArgumentException("id cannot be null");
    }

    String ingredientSql =
"""
  select i.id as i_id, i.name as i_name, i.price as i_price, i.category as i_category from ingredient i where i.id_dish = ?
""";

    Connection con = null;
    PreparedStatement ingStmt = null;
    ResultSet ingRs = null;

    try {
      con = dbConnection.getDBConnection();
      ingStmt = con.prepareStatement(ingredientSql);
      ingStmt.setInt(1, id);
      ingRs = ingStmt.executeQuery();

      List<Ingredient> ingredients = new ArrayList<>();
      while (ingRs.next()) {
        ingredients.add(mapIngredientFromResultSet(ingRs));
      }

      return ingredients;

    } catch (SQLException e) {
      throw new RuntimeException("Error while fetching ingredients", e);
    } finally {
      dbConnection.attemptCloseDBConnection(ingRs, ingStmt, con);
    }
  }

  private Ingredient mapIngredientFromResultSet(ResultSet ingRs) throws SQLException {
    Ingredient ingredient = new Ingredient();
    ingredient.setId(ingRs.getInt("i_id"));
    ingredient.setName(ingRs.getString("i_name"));
    ingredient.setPrice(ingRs.getDouble("i_price"));
    ingredient.setCategory(CategoryEnum.valueOf(ingRs.getString("i_category")));
    return ingredient;
  }

  private Dish mapDishFromResultSet(ResultSet dishRs) throws SQLException {
    Dish dish = new Dish();
    dish.setId(dishRs.getInt("d_id"));
    dish.setName(dishRs.getString("d_name"));
    dish.setDishType(DishTypeEnum.valueOf(dishRs.getString("dish_type")));
    return dish;
  }

  @Override
  public Dish saveDish(Dish dish) {
    if (dish == null) {
      throw new IllegalArgumentException("Dish cannot be null");
    }

    isValid(dish);

    String saveDishSql =
"""
    insert into dish (id, name, dish_type)
    values (?, ?, ?::dish_type)
    on conflict do update
    set name = excluded.name, dish_type = excluded.dish_type
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
      saveDishRs = saveDishStmt.executeQuery();
      saveDishRs.next();
      int dishId = saveDishRs.getInt(1);

      List<Ingredient> newIngredients = dish.getIngredients();
      detachIngredients(con, dishId, newIngredients);
      attachIngredients(con, dishId, newIngredients);

      con.commit();
      return findDishById(dishId);
    } catch (SQLException e) {
      try {
        if (con != null && !con.isClosed()) {
          con.rollback();
        }
      } catch (SQLException ex) {
        throw new RuntimeException("Failed to rollaback", ex);
      }
      throw new RuntimeException("Failed to save new dish", e);
    } finally {
      try {
        if (con != null && !con.isClosed()) {
          con.setAutoCommit(true);
        }
      } catch (SQLException ex) {
        System.out.println("Failed to set autocommit to true");
      }
      dbConnection.attemptCloseDBConnection(saveDishRs, saveDishStmt, con);
    }
  }

  private void detachIngredients(Connection conn, Integer dishId, List<Ingredient> ingredients)
      throws SQLException {
    if (ingredients == null || ingredients.isEmpty()) {
      try (PreparedStatement ps =
          conn.prepareStatement("update ingredient set id_dish = null where id_dish = ?")) {
        ps.setInt(1, dishId);
        ps.executeUpdate();
      }
      return;
    }

    String baseSql =
        """
                    update ingredient
                    set id_dish = null
                    where id_dish = ? and id not int (%s)
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
                    update ingredient
                    set id_dish = ?
                    where id = ?
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

  @Override
  public List<Dish> findDishesByIngredientName(String ingredientName) {
    return List.of();
  }

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
      throw new RuntimeException("Error while trying to fetch ingredients", e);
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
      try {
        if (con != null) {
          if (!con.isClosed()) {
            con.rollback();
          }
        }
      } catch (SQLException ex) {
        throw new RuntimeException("Failed to rollback", ex);
      }
      throw new RuntimeException("Error while creating ingredients", e);
    } finally {
      try {
        if (con != null) {
          if (!con.isClosed()) {
            con.setAutoCommit(true);
          }
        }
      } catch (SQLException e) {
        System.out.println("Failed to set autoCommit to true");
      }
      dbConnection.attemptCloseDBConnection(createIngRs, createIngStmt, con);
    }
  }

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
      throw new RuntimeException("Failed to get next value", e);
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
            "SELECT setval('%s', (SELECT COALESCE(MAX(%s),0) FROM %s))",
            sequenceName, columnName, tableName);
    Statement updateSeqStmt = null;
    try {
      updateSeqStmt = con.createStatement();
      updateSeqStmt.executeQuery(updateSeqSql);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to update sequence", e);
    } finally {
      dbConnection.attemptCloseDBConnection(updateSeqStmt);
    }
  }

  private Ingredient findIngredientById(Integer id) {
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
      return mapIngredientFromResultSet(findIngRs);
    } catch (SQLException e) {
      throw new RuntimeException("Error while fetching ingredient", e);
    } finally {
      dbConnection.attemptCloseDBConnection(findIngRs, findIngStmt, con);
    }
  }

  private void isValid(Ingredient newIngredient) {
    if (newIngredient.getName() == null || newIngredient.getName().isBlank()) {
      throw new IllegalArgumentException("Ingredient name cannot be null or empty");
    }
    if (newIngredient.getCategory() == null) {
      throw new IllegalArgumentException("Ingredient category cannot be null");
    }
    if (newIngredient.getPrice() == null || newIngredient.getPrice() <= 0) {
      throw new IllegalArgumentException("Ingredient price cannot be null or negative");
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
        ingredients.add(mapIngredientFromResultSet(findIngRs));
      }
      return ingredients;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      dbConnection.attemptCloseDBConnection(findIngRs, findIngStmt, con);
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
        hasWhere = true;
      }
      sqlBuilder.append(
              "exists (select 1 from dish_ingredient di join dish d on di.id_dish = d.id where di.id_ingredient = i.id and d.name ilike ?)");
    }

    sqlBuilder.append(" order by i.id limit ? offset ?");

    return sqlBuilder.toString();
  }

}
