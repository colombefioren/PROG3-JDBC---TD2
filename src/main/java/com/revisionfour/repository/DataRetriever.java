package com.revisionfour.repository;

import com.revisionfour.db.DBConnection;
import com.revisionfour.model.CategoryEnum;
import com.revisionfour.model.Dish;
import com.revisionfour.model.Ingredient;

import java.util.List;

public class DataRetriever implements IngredientRepository, DishRepository{
    private final DBConnection dbConnection;

    public DataRetriever(){
        this.dbConnection = new DBConnection();
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
    public List<Ingredient> findIngredientsByCriteria(String ingredientName, CategoryEnum category, String dishName, int page, int size) {
        return List.of();
    }
}
