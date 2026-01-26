package com.revisionfour.repository;

import com.revisionfour.model.DishIngredient;
import java.util.List;

public interface DishIngredientRepository {
  List<DishIngredient> findDishIngredientsByDishId(Integer id);

  DishIngredient saveDishIngredient(DishIngredient newDishIngredient);

  DishIngredient findDishIngredientById(Integer id);
}
