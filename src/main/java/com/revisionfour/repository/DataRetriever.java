package com.revisionfour.repository;

import com.revisionfour.db.DBConnection;
import com.revisionfour.model.CategoryEnum;
import com.revisionfour.model.Dish;
import com.revisionfour.model.Ingredient;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
    return null;
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
    return List.of();
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
