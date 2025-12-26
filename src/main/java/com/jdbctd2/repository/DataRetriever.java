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

  public void initializeDB() {
    String clearTablesSql =
"""
truncate table Ingredient, Dish restart identity cascade
""";

    String insertDishSql =
"""
insert into Dish (name, dish_type) values
        ('Salade fraîche', 'START'),
        ('Poulet grillé', 'MAIN'),
        ('Riz aux légumes', 'MAIN'),
        ('Gâteau au chocolat', 'DESSERT'),
        ('Salade de fruits', 'DESSERT')
""";

    String insertIngredientSql =
"""
insert into Ingredient (name, price, category, id_dish) values
        ('Laitue', 800.00, 'VEGETABLE', 1),
        ('Tomate', 600.00, 'VEGETABLE', 1),
        ('Poulet', 4500.00, 'ANIMAL', 2),
        ('Chocolat', 3000.00, 'OTHER', 4),
        ('Beurre', 2500.00, 'DAIRY', 4)
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
      dbConnection.attemptCloseDBConnection(con, stmt);
    }
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
                    order by dish_id
                   """;

    String ingredientSql =
"""
select i.id as ing_id, i.name as ing_name, i.price as ing_price, i.category as ing_category
from Ingredient i
where i.id_dish = ?
order by ing_id
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
    if (dishToSave == null) {
      throw new IllegalArgumentException("Dish to save cannot be null");
    }

    isValid(dishToSave);

    String findDishSql =
        """
  select id as dish_id from Dish d where d.id = ?
  """;

    String updateDishSql =
"""
update Dish d set name = ?, dish_type = ?::dish_type where d.id = ?
""";

    String createDishSql =
"""
insert into Dish (name, dish_type) values (?, ?::dish_type) returning id
""";

    String dissociateSql =
"""
update Ingredient set id_dish = null where id_dish = ?
""";

    String associateSql =
"""
update Ingredient set id_dish = ? where id = ?
""";

    Connection con = null;
    PreparedStatement findDishStmt = null;
    ResultSet findDishRs = null;
    PreparedStatement updateDishStmt = null;
    PreparedStatement createDishStmt = null;
    ResultSet createDishRs;
    PreparedStatement dissociateStmt = null;
    PreparedStatement associateStmt = null;

    try {
      con = dbConnection.getDBConnection();
      con.setAutoCommit(false);
      Integer savedDishId = null;
      boolean isUpdate = false;

      if (dishToSave.getId() != null) {
        findDishStmt = con.prepareStatement(findDishSql);
        findDishStmt.setInt(1, dishToSave.getId());
        findDishRs = findDishStmt.executeQuery();
        if (findDishRs.next()) {
          savedDishId = findDishRs.getInt("dish_id");
          isUpdate = true;
        }
      }

      if (isUpdate) {
        updateDishStmt = con.prepareStatement(updateDishSql);
        updateDishStmt.setString(1, dishToSave.getName());
        updateDishStmt.setString(2, dishToSave.getDishType().name());
        updateDishStmt.setInt(3, dishToSave.getId());
        int rowsUpdated = updateDishStmt.executeUpdate();
        if (rowsUpdated == 0) {
          throw new RuntimeException("Error while updating dish with id " + dishToSave.getId());
        }
      } else {
        createDishStmt = con.prepareStatement(createDishSql, Statement.RETURN_GENERATED_KEYS);
        createDishStmt.setString(1, dishToSave.getName());
        createDishStmt.setString(2, dishToSave.getDishType().name());
        createDishStmt.executeUpdate();
        createDishRs = createDishStmt.getGeneratedKeys();
        if (createDishRs.next()) {
          savedDishId = createDishRs.getInt(1);
        } else {
          throw new RuntimeException("Error while creating dish with name " + dishToSave.getName());
        }
      }

      if (isUpdate) {
        dissociateStmt = con.prepareStatement(dissociateSql);
        dissociateStmt.setInt(1, savedDishId);
        dissociateStmt.executeUpdate();
      }

      if (dishToSave.getIngredients() != null && !dishToSave.getIngredients().isEmpty()) {
        associateStmt = con.prepareStatement(associateSql);
        for (Ingredient ingredient : dishToSave.getIngredients()) {
          associateStmt.setInt(1, savedDishId);
          associateStmt.setInt(2, ingredient.getId());
          associateStmt.addBatch();
        }

        int[] batchResults = associateStmt.executeBatch();
        for (int result : batchResults) {
          if (result == Statement.EXECUTE_FAILED) {
            throw new RuntimeException(
                "Error while associating ingredients to dish with id " + dishToSave.getId());
          }
        }
      }

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
      dbConnection.attemptCloseDBConnection(
          con,
          findDishStmt,
          findDishRs,
          updateDishStmt,
          createDishStmt,
          dissociateStmt,
          associateStmt);
    }
  }

  @Override
  public List<Dish> findDishesByIngredientName(String IngredientName) {
    String findIngSql =
"""
    select d.id as dish_id, d.name as dish_name, d.dish_type, i.name as ing_name
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
      dbConnection.attemptCloseDBConnection(con, findIngRs, findIngSmt);
    }
  }

  @Override
  public Ingredient findIngredientByName(String ingredientName) {
    String findIngByNameSql =
"""
select i.id as ing_id, i.name as ing_name, i.name as ing_name, i.price as ing_price, i.category as ing_category, i.id_dish as id_dish
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
      dbConnection.attemptCloseDBConnection(con, findIngByNameStmt, findIngByNameRs);
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
insert into ingredient (name, price, category, id_dish) values (?, ?, ?::category, ?)
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

      createIngStmt = con.prepareStatement(createIngSql, Statement.RETURN_GENERATED_KEYS);
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
      dbConnection.attemptCloseDBConnection(
          con, createIngStmt, findIngStmt, findIngRs, generatedKeys);
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
      dbConnection.attemptCloseDBConnection(con, findIngRs, findIngStmt);
    }
  }

  private static String getFindIngSql(
      String ingredientName, CategoryEnum category, String dishName) {
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
}
