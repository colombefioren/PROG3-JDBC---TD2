package com.jdbctd2;

import com.jdbctd2.model.*;
import com.jdbctd2.repository.DataRetriever;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    dataRetriever.initializeDB();

    System.out.println("\n===> Stock::getQuantity <===");
    List<Ingredient> allIngredients = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      try {
        allIngredients.add(dataRetriever.findIngredientById(i));

      } catch (RuntimeException e) {
        System.out.println("Ingredient with id " + i + " not found: " + e.getMessage());
      }
    }

    for (Ingredient ingredient : allIngredients) {
      System.out.println("\nIngredient name : " + ingredient.getName());
      System.out.println(
          "Stock : " + ingredient.getStockValueAt(Instant.parse("2024-01-06T12:00:00Z")));
    }
  }
}
