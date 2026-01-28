package com.jdbctd2.repository;

import com.jdbctd2.model.Dish;
import java.util.List;

public interface DishRepository {
  Dish findDishById(Integer id);

  Dish saveDish(Dish dish);

  List<Dish> findDishesByIngredientName(String ingredientName);
}
