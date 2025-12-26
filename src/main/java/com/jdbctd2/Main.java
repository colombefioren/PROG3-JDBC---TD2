package com.jdbctd2;

import com.jdbctd2.model.Dish;
import com.jdbctd2.model.Ingredient;
import com.jdbctd2.repository.DataRetriever;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    // a) Dish findDishById(Integer id)
    System.out.println("===> Dish findDishById(Integer id) <===");
    Dish dish = dataRetriever.findDishById(1);
    System.out.println("id=1 : " + dish);

    // b) List<Ingredient> findIngredients(int page, int size)
    System.out.println("\n===> List<Ingredient> findIngredients(int page, int size) <===");
    List<Ingredient> ingredientList = dataRetriever.findIngredients(2, 2);
    System.out.println("page=2,size=2 : " + ingredientList);
  }
}
