package com.jdbctd2.repository;

import com.jdbctd2.db.DBConnection;
import com.jdbctd2.model.CategoryEnum;
import com.jdbctd2.model.Dish;
import com.jdbctd2.model.DishTypeEnum;
import com.jdbctd2.model.Ingredient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever implements IngredientRepository, DishRepository {

  private final DBConnection dbConnection;

  public DataRetriever() {
    this.dbConnection = new DBConnection();
  }

  @Override
  public Dish findDishById(Integer id) {
    if(id == null || id <= 0){
      throw new IllegalArgumentException("Dish id must be positive");
    }
    String dishSql =
        """
                    select d.id as dish_id, d.name as dish_name, d.dish_type
                    from Dish d
                    where d.id = ?;
                   """;

    String ingredientSql =
"""
select i.id, i.name, i.price, i.category
from Ingredient i
where i.id_dish = ?;
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
      ingredientRs = ingredientStmt.executeQuery();

      List<Ingredient> ingredients = new ArrayList<>();
      while (ingredientRs.next()) {
        Ingredient ingredient = new Ingredient();
        ingredient.setId(ingredientRs.getInt("id"));
        ingredient.setName(ingredientRs.getString("name"));
        ingredient.setPrice(ingredientRs.getDouble("price"));
        ingredient.setCategory(CategoryEnum.valueOf(ingredientRs.getString("category")));
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
    String sql =
"""
        select ingredient.id, ingredient.name, ingredient.price, ingredient.category, ingredient.id_dish as dish_id
        from ingredient
        order by ingredient.id
        limit ? offset ?
""";

    int offset = (page - 1) * size;
    List<Ingredient> result = new ArrayList<>();
    Dish dish = null;

    try (Connection con = dbConnection.getDBConnection();
        PreparedStatement ps = con.prepareStatement(sql)) {

      ps.setInt(1, size);
      ps.setInt(2, offset);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          dish = findDishById(rs.getInt("dish_id"));
          result.add(createIngredientFromResultSet(rs, dish));
        }
      }

    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
    return result;
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
