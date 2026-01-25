package com.revisionfour.repository;

import com.revisionfour.model.Dish;

import java.util.List;

public interface DishRepository {
    Dish findDishById(Integer id);
    Dish saveDish(Dish dish);
    List<Dish> findDishesByIngredientName(String ingredientName);
}
