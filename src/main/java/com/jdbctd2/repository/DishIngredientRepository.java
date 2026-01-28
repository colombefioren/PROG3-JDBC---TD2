package com.jdbctd2.repository;

import com.jdbctd2.model.DishIngredient;
import java.util.List;

public interface DishIngredientRepository {
  List<DishIngredient> findDishIngredientsByDishId(Integer id);

  DishIngredient saveDishIngredient(DishIngredient newDishIngredient);

  DishIngredient findDishIngredientById(Integer id);
}
