package com.jdbctd2.repository;

import com.jdbctd2.db.DBConnection;
import com.jdbctd2.model.CategoryEnum;
import com.jdbctd2.model.Dish;
import com.jdbctd2.model.DishTypeEnum;
import com.jdbctd2.model.Ingredient;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataRetriever implements IngredientRepository, DishRepository {

  private final DBConnection dbConnection;

  public DataRetriever() {
    this.dbConnection = new DBConnection();
  }

  @Override
  public Dish findDishById(Integer id) {
    if (id == null || id <= 0) {
      throw new IllegalArgumentException("Dish id must be positive");
    }
    String dishSql =
        """
                    select d.id as dish_id, d.name as dish_name, d.dish_type
                    from Dish d
                    where d.id = ?
                    order by dish_id;
                   """;

    String ingredientSql =
"""
select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category
from Ingredient i
where i.id_dish = ?
order by ing_id;
""";

    Connection con = null;
    PreparedStatement dishStmt = null;
    ResultSet dishRs = null;
    PreparedStatement ingredientStmt = null;
    ResultSet ingredientRs = null;

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

      ingredientStmt = con.prepareStatement(ingredientSql);
      ingredientStmt.setInt(1, id);
      ingredientRs = ingredientStmt.executeQuery();

      List<Ingredient> ingredients = new ArrayList<>();
      while (ingredientRs.next()) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientRs.getInt("ing_id"));
        ingredient.setName(ingredientRs.getString("ing_name"));
        ingredient.setPrice(ingredientRs.getDouble("ing_price"));
        ingredient.setCategory(CategoryEnum.valueOf(ingredientRs.getString("ing_category")));
        ingredients.add(ingredient);
      }

      dish.setIngredients(ingredients);
      return dish;
    } catch (SQLException e) {
      throw new RuntimeException("Error while trying to retrieve dish with id " + id + e);
    } finally {
      dbConnection.attemptCloseDBConnection(con, dishStmt, dishRs, ingredientStmt, ingredientRs);
    }
  }

  @Override
  public Dish saveDish(Dish dishToSave) {
    return null;
  }

  @Override
  public List<Dish> findDishesByIngredientName(String IngredientName) {
    return List.of();
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
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientRs.getInt("ing_id"));
        ingredient.setName(ingredientRs.getString("ing_name"));
        ingredient.setPrice(ingredientRs.getDouble("ing_price"));
        ingredient.setCategory(CategoryEnum.valueOf(ingredientRs.getString("ing_category")));
        ingredient.setDish(findDishById(ingredientRs.getInt("id_dish")));
        ingredients.add(ingredient);
      }

      return ingredients;
    } catch (SQLException e) {
      throw new RuntimeException("Error while trying to fetch ingredients", e);
    } finally {
      dbConnection.attemptCloseDBConnection(con, ingredientStmt, ingredientRs);
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

    String createIngSql =
"""
insert into ingredient (name, price, category, id_dish) values (?, ?, ?::category, ?);
""";

    String findIngSql =
"""
select i.id as ing_id, i.name as ing_name from ingredient i where lower(i.name) = lower(?)
""";

    Connection con = null;
    PreparedStatement createIngStmt = null;
    PreparedStatement findIngStmt = null;
    ResultSet findIngRs = null;
    ResultSet generatedKeys = null;
    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      findIngStmt = con.prepareStatement(findIngSql);

      for (Ingredient newIngredient : newIngredients) {
        findIngStmt.setString(1, newIngredient.getName());
        findIngRs = findIngStmt.executeQuery();
        if (findIngRs.next()) {
          con.rollback();
          throw new RuntimeException("Ingredient already exists: " + newIngredient.getName());
        }
      }

      createIngStmt = con.prepareStatement(createIngSql);
      for (Ingredient newIngredient : newIngredients) {
        createIngStmt.setString(1, newIngredient.getName());
        createIngStmt.setDouble(2, newIngredient.getPrice());
        createIngStmt.setString(3, newIngredient.getCategory().name());

        if (newIngredient.getDish() != null) {
          createIngStmt.setInt(4, newIngredient.getDish().getId());
        } else {
          createIngStmt.setNull(4, Types.INTEGER);
        }
        createIngStmt.addBatch();
      }
      int[] batchResults = createIngStmt.executeBatch();

      for (int i = 0; i < batchResults.length; i++) {
        if (batchResults[i] == Statement.EXECUTE_FAILED) {
          con.rollback();
          throw new RuntimeException(
              "Error while creating ingredient: "
                  + newIngredients.get(i).getName()
                  + ". No ingredients are thus inserted!");
        }
      }
      generatedKeys = createIngStmt.getGeneratedKeys();
      List<Ingredient> createdIngredients = new ArrayList<>();
      int index = 0;
      while (generatedKeys.next()) {
        Ingredient createdIngredient = new Ingredient();
        createdIngredient.setId(generatedKeys.getInt(1));
        createdIngredient.setName(newIngredients.get(index).getName());
        createdIngredient.setPrice(newIngredients.get(index).getPrice());
        createdIngredient.setCategory(newIngredients.get(index).getCategory());
        createdIngredient.setDish(newIngredients.get(index).getDish());
        createdIngredients.add(createdIngredient);
        index++;
      }
      con.commit();
      return createdIngredients;
    } catch (SQLException e) {
      try {
        if (con != null && !con.isClosed()) {
          con.rollback();
          System.out.println("An error occured so that transaction was rolled back");
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
      dbConnection.attemptCloseDBConnection(
          con, createIngStmt, findIngStmt, findIngRs, generatedKeys);
    }
  }

  @Override
  public List<Ingredient> findIngredientsByCriteria(
      String ingredientName, CategoryEnum category, String dishName, int page, int size) {
    return List.of();
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
}
