package com.jdbctd2;

import com.jdbctd2.model.Dish;
import com.jdbctd2.model.Ingredient;
import com.jdbctd2.repository.DataRetriever;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    // a) Dish findDishById(Integer id)
    System.out.println("===> Dish findDishById(Integer id) <===");
    Dish dish = dataRetriever.findDishById(1);
    System.out.println("Dish with ID = 1 : " + dish);
  }
}
