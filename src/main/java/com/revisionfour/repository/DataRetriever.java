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
    }finally{
      dbConnection.attemptCloseDBConnection(findIngRs, findIngStmt, con);
    }
  }

  @Override
  public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
    return List.of();
  }

  @Override
  public List<Ingredient> findIngredientsByCriteria(
      String ingredientName, CategoryEnum category, String dishName, int page, int size) {
    return List.of();
  }
}
