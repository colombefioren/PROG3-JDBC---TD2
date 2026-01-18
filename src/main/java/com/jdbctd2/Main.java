package com.jdbctd2;

import com.jdbctd2.model.CategoryEnum;
import com.jdbctd2.model.Dish;
import com.jdbctd2.model.DishTypeEnum;
import com.jdbctd2.model.Ingredient;
import com.jdbctd2.repository.DataRetriever;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    // BeforeAll
    dataRetriever.initializeDB();

    // a) Dish findDishById(Integer id) - id = 1
    System.out.println("===> Dish findDishById(Integer id) | id = 1 <===");
    Dish dishA = dataRetriever.findDishById(1);
    System.out.println("id=1 : " + dishA);

    // b) Dish findDishById(Integer id) - id = 999
    System.out.println("\n===> Dish findDishById(Integer id) | id = 999 <===");
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

    // e) List<Ingredient> findDishesByIngredientName(String IngredientName) - eur
    System.out.println(
        "\n===> List<Ingredient> findDishesByIngredientName(String IngredientName) | eur <===");
    List<Dish> dishesByIngredientNameE = dataRetriever.findDishesByIngredientName("eur");
    System.out.println("dishesByIngredientName : " + dishesByIngredientNameE);

    // f) List<Ingredient> findIngredientsByCriteria(...) - ingredientName=null category=VEGETABLE
    // dishName=null page=1 size=10
    System.out.println(
        "\n===> List<Ingredient> findIngredientsByCriteria(...) | ingredientName=null category=VEGETABLE dishName=null page=1 size=10 <===");
    List<Ingredient> ingredientListF =
        dataRetriever.findIngredientsByCriteria(null, CategoryEnum.VEGETABLE, null, 1, 10);
    System.out.println("ingredientList : " + ingredientListF);

    // g) List<Ingredient> findIngredientsByCriteria(...) - ingredientName=cho category=null
    // dishName=Sal page=1 size=10
    System.out.println(
        "\n===> List<Ingredient> findIngredientsByCriteria(...) | ingredientName=cho category=null dishName=Sal page=1 size=10 <===");
    List<Ingredient> ingredientListG =
        dataRetriever.findIngredientsByCriteria("cho", null, "Sal", 1, 10);
    System.out.println("ingredientList : " + ingredientListG);

    // h) List<Ingredient> findIngredientsByCriteria(...) - ingredientName=cho category=null
    // dishName=gâteau page=1 size=10
    System.out.println(
        "\n===> List<Ingredient> findIngredientsByCriteria(...) | ingredientName=cho category=null dishName=gâteau page=1 size=10");
    List<Ingredient> ingredientListH =
        dataRetriever.findIngredientsByCriteria("cho", null, "gâteau", 1, 10);
    System.out.println("ingredientList : " + ingredientListH);

    // i) List<Ingredient> createIngredient(...) - fromage and oignon
    System.out.println("\n===> List<Ingredient> createIngredient(...) | fromage and oignon <===");
    Ingredient fromage = new Ingredient("Fromage", 1200.0, CategoryEnum.DAIRY);
    Ingredient oignon = new Ingredient("Oignon", 500.0, CategoryEnum.VEGETABLE);
    try {
      List<Ingredient> createdIngredientsI =
          dataRetriever.createIngredients(new ArrayList<>(Arrays.asList(fromage, oignon)));
      System.out.println("createdIngredients : " + createdIngredientsI);
    } catch (RuntimeException e) {
      System.out.println("Error while creating ingredients : " + e);
    }

    // j) List<Ingredient> createIngredient(...) - carotte and laitue
    System.out.println("\n===> List<Ingredient> createIngredient(...) | carotte and laitue <===");
    Ingredient carotte = new Ingredient("Carotte", 2000.0, CategoryEnum.VEGETABLE);
    Ingredient laitue = new Ingredient(1, "Laitue", 2000.0, CategoryEnum.VEGETABLE, null);
    try {
      List<Ingredient> createdIngredientsJ =
          dataRetriever.createIngredients(new ArrayList<>(Arrays.asList(carotte, laitue)));
      System.out.println("createdIngredients : " + createdIngredientsJ);
    } catch (RuntimeException e) {
      System.out.println("Error while creating ingredients : " + e);

      // k) Dish saveDish(...) - soupe de légumes
      System.out.println(
          "\n===> Dish saveDish(...) | name=Soupe de légumes dishType=START ingredients=Oignon <===");
      Ingredient oignonIngredient = dataRetriever.findIngredientByName("oignon");
      Dish newDishK =
          new Dish(
              "Soupe de légumes",
              DishTypeEnum.START,
              new ArrayList<>(Collections.singletonList(oignonIngredient)));
      Dish savedDishK = dataRetriever.saveDish(newDishK);
      System.out.println("savedDish : " + savedDishK);

      // l) Dish saveDish(...) - salade fraîche
      System.out.println(
          "\n===> Dish saveDish(...) | id=1 name=Salade fraîche dishType=START ingredients=Oignon, Laitue, Tomate, Fromage <===");
      Ingredient laitueIngredient = dataRetriever.findIngredientByName("laitue");
      Ingredient fromageIngredient = dataRetriever.findIngredientByName("fromage");
      Ingredient tomateIngredient = dataRetriever.findIngredientByName("tomate");
      Dish newDishL =
          new Dish(
              1,
              "Salade fraîche",
              DishTypeEnum.START,
              new ArrayList<>(
                  Arrays.asList(
                      oignonIngredient, laitueIngredient, tomateIngredient, fromageIngredient)));
      Dish savedDishL = dataRetriever.saveDish(newDishL);
      System.out.println("savedDish : " + savedDishL);

      // m) Dish saveDish(...) - Salade de fromage
      System.out.println(
          "\n===> Dish saveDish(...) | id=1 name=Salade de fromage dishType=START ingredients=Fromage <===");
      Dish newDishM =
          new Dish(
              1,
              "Salade de fromage",
              DishTypeEnum.START,
              new ArrayList<>(Collections.singletonList(fromageIngredient)));
      Dish savedDishM = dataRetriever.saveDish(newDishM);
      System.out.println("savedDish : " + savedDishM);
    }

    System.out.println("\n===> findDishById avec getGrossMargin | 800 <===");

    // dish of id=1 and price=2000 | 2000 - 1200 = 800
    try {
      Dish dish1 = dataRetriever.findDishById(1);
      System.out.println("Dish: " + dish1.getName());
      System.out.println("Price: " + dish1.getPrice());
      System.out.println("Ingredient prices: " + dish1.getDishCost());
      System.out.println("Gross Margin: " + dish1.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }

    // savedish and update price of id=3 to be 3500
    System.out.println("\n===> saveDish update the dish of id 3 price to 3500 <===");

    Dish dishToUpdate = dataRetriever.findDishById(3);
    System.out.println(dishToUpdate); // Riz aux légumes
    System.out.println("Ancient dish price: " + dishToUpdate.getPrice());
    try {
      System.out.println("Gross Margin before update: " + dishToUpdate.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }
    dishToUpdate.setPrice(3500.0);
    Dish updatedDish = dataRetriever.saveDish(dishToUpdate);
    System.out.println("New dish price: " + updatedDish.getPrice());

    try {
      System.out.println("Gross Margin after update: " + updatedDish.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }

    System.out.println("\n===> saveDish create a new dish with price <===");
    Ingredient laitueIngredient = dataRetriever.findIngredientByName("laitue");
    Ingredient tomateIngredient = dataRetriever.findIngredientByName("tomate");
    Dish newDish =
        new Dish(
            "Salade César",
            DishTypeEnum.START,
            new ArrayList<>(Arrays.asList(laitueIngredient, tomateIngredient)));
    newDish.setPrice(2500.0);

    Dish savedNewDish = dataRetriever.saveDish(newDish);
    System.out.println(savedNewDish);

    try {
      System.out.println("Gross Margin of the new dish: " + savedNewDish.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }

    System.out.println("\n===> saveDish Dish without price <===");
    Dish dishWithoutPrice = new Dish("Soupe du jour", DishTypeEnum.START, new ArrayList<>());

    Dish savedWithoutPrice = dataRetriever.saveDish(dishWithoutPrice);
    System.out.println("Dish: " + savedWithoutPrice.getName());
    System.out.println("Price: " + savedWithoutPrice.getPrice());

    try {
      System.out.println("Gross Margin: " + savedWithoutPrice.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }

    // initialize the db at the end as well
    dataRetriever.initializeDB();
  }
}
