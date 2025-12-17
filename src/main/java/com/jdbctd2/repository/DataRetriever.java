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

public class DataRetriever implements IngredientRepository, DishRepository{

    private final DBConnection dbConnection;

    public DataRetriever() {
        this.dbConnection = new DBConnection();
    }

    @Override
    public Dish findDishById(Integer id) {
        String sql = """
                    select ingredient.id, ingredient.price, ingredient.category,ingredient.name,
                   dish.id as dish_id, dish.name as dish_name, dish.dish_type as dish_type
                    from ingredient right join dish on dish.id = ingredient.id_dish
                    where id_dish = ?;
                   """;

        try(Connection con = dbConnection.getDBConnection();
            PreparedStatement ps = con.prepareStatement(sql)){
                ps.setInt(1,id);
                ResultSet rs = ps.executeQuery();
                return createDishFromResultSet(rs);

        }catch (SQLException e){
            throw new RuntimeException(e);
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
        String
        return List.of();
    }

    @Override
    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {
        return List.of();
    }

    @Override
    public List<Ingredient> findIngredientsByCriteria(String ingredientNmae, CategoryEnum category, String dishName, int page, int size) {
        return List.of();
    }

    private Dish createDishFromResultSet(ResultSet rs) throws SQLException {
        Dish dish = null;
        List<Ingredient> ingredients = new ArrayList<>();

        while (rs.next()) {
            if (dish == null) {
                dish = new Dish();
                dish.setId(rs.getInt("dish_id"));
                dish.setName(rs.getString("dish_name"));
                dish.setDishType(
                        DishTypeEnum.valueOf(rs.getString("dish_type"))
                );
            }
            ingredients.add(createIngredientFromResultSet(rs, dish));
        }

        if (dish != null) {
            dish.setIngredients(ingredients);
        }

        return dish;

    }

    private Ingredient createIngredientFromResultSet(ResultSet rs, Dish dish) throws SQLException {
        Ingredient ing = new Ingredient();
        ing.setId(rs.getInt("id"));
        ing.setName(rs.getString("name"));
        ing.setPrice(rs.getDouble("price"));
        ing.setCategory(
                CategoryEnum.valueOf(rs.getString("category"))
        );

       ing.setDish(dish);

        return ing;
    }
}
