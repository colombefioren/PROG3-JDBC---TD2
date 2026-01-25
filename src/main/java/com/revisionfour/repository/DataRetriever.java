package com.revisionfour.repository;

import com.revisionfour.db.DBConnection;
import com.revisionfour.model.CategoryEnum;
import com.revisionfour.model.Dish;
import com.revisionfour.model.DishTypeEnum;
import com.revisionfour.model.Ingredient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
    return null;
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
      isValid(newIngredient);
    }

    String createIngSql =
"""
    insert into ingredient (id, name, price, category)
    values (?, ?, ?, ?::category)
    on conflict (id)
    do update
    set price = excluded.price,
    category = excluded.category,
    name = excluded.name
    returning id;
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
        createdIngredients.add(findIngredientById(createIngRs.getInt(1)));
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
    String getNextValueSql =
"""
 select nextval('" + sequenceName + "')
""";
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

  private Ingredient findIngredientById(int id) {
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
    return List.of();
  }
}
