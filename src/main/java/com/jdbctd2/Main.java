package com.jdbctd2;

import com.jdbctd2.model.Dish;
import com.jdbctd2.model.Ingredient;
import com.jdbctd2.repository.DataRetriever;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    // a) Dish findDishById(Integer id) - id = 1
    System.out.println("===> Dish findDishById(Integer id) | id = 1 <===");
    Dish dishA = dataRetriever.findDishById(1);
    System.out.println("id=1 : " + dishA);

    // b) Dish findDishById(Integer id) - id = 1
    System.out.println("===> Dish findDishById(Integer id) | id = 999 <===");
    try {
      Dish dishB = dataRetriever.findDishById(999);
      System.out.println("id=1 : " + dishB);
    } catch (RuntimeException e) {
      System.out.println("Dish with id 999 not found. Error: " + e);
    }

    // c) List<Ingredient> findIngredients(int page, int size) - page=2 size=2
    System.out.println(
        "\n===> List<Ingredient> findIngredients(int page, int size) | page=2,size=2 <===");
    List<Ingredient> ingredientListC = dataRetriever.findIngredients(2, 2);
    System.out.println("page=2,size=2 : " + ingredientListC);

    // d) List<Ingredient> findIngredients(int page, int size) - page=3 size=5
    System.out.println(
        "\n===> List<Ingredient> findIngredients(int page, int size) | page=3,size=5 <===");
    List<Ingredient> ingredientListD = dataRetriever.findIngredients(3, 5);
    System.out.println("page=2,size=2 : " + ingredientListD);
  }
}
