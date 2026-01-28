package com.jdbctd2.repository;

import com.jdbctd2.model.CategoryEnum;
import com.jdbctd2.model.Ingredient;
import java.util.List;

public interface IngredientRepository {
  List<Ingredient> findIngredients(int page, int size);

  List<Ingredient> createIngredients(List<Ingredient> newIngredients);

  List<Ingredient> findIngredientsByCriteria(
      String ingredientName, CategoryEnum category, String dishName, int page, int size);

  Ingredient findIngredientByName(String ingredientName);

  Ingredient findIngredientById(Integer id);

  Ingredient saveIngredient(Ingredient toSave);
}
