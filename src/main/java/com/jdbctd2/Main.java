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
    Ingredient laitue = new Ingredient("Laitue", 2000.0, CategoryEnum.VEGETABLE);
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

    System.out.println("=== Test 1: findDishById avec getGrossMargin ===");

    // test with dish with price 2000 (id=1)
    try {
      Dish dish1 = dataRetriever.findDishById(1);
      System.out.println("Dish 1: " + dish1.getName());
      System.out.println("Prix: " + dish1.getPrice());
      System.out.println("Coût des ingrédients: " + dish1.getDishCost());
      System.out.println("Marge brute: " + dish1.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Erreur: " + e.getMessage());
    }

    //  test with dish without id (id=3)
    try {
      Dish dish3 = dataRetriever.findDishById(3);
      System.out.println("\nDish 3: " + dish3.getName());
      System.out.println("Prix: " + dish3.getPrice());
      System.out.println("Coût des ingrédients: " + dish3.getDishCost());
      System.out.println("Marge brute: " + dish3.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Erreur attendue: " + e.getMessage());
    }

    // test save dish to update the price
    System.out.println("\n=== Test 2: saveDish avec mise à jour du prix ===");

    // update the price of an existing dish
    Dish dishToUpdate = dataRetriever.findDishById(3); // Riz aux légumes
    dishToUpdate.setPrice(3500.0);
    Dish updatedDish = dataRetriever.saveDish(dishToUpdate);
    System.out.println("Plat mis à jour: " + updatedDish.getName());
    System.out.println("Nouveau prix: " + updatedDish.getPrice());

    // test the margin after the update
    try {
      System.out.println("Marge brute après mise à jour: " + updatedDish.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Erreur: " + e.getMessage());
    }

    // create new dish with price
    System.out.println("\n=== Test 3: Créer un nouveau plat avec prix ===");

    Dish newDish = new Dish("Salade César", DishTypeEnum.START, new ArrayList<>(List.of(laitue)));
    newDish.setPrice(2500.0);

    Dish savedNewDish = dataRetriever.saveDish(newDish);
    System.out.println(savedNewDish);

    // test margin of that new dish
    try {
      System.out.println("Marge brute du nouveau plat: " + savedNewDish.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Erreur: " + e.getMessage());
    }

    // test with dish without price
    System.out.println("\n=== Test 4: Plat sans prix ===");
    Dish dishWithoutPrice = new Dish("Soupe du jour", DishTypeEnum.START, new ArrayList<>());
    // dont define price

    Dish savedWithoutPrice = dataRetriever.saveDish(dishWithoutPrice);
    System.out.println("Plat sans prix créé: " + savedWithoutPrice.getName());
    System.out.println("Prix: " + savedWithoutPrice.getPrice());

    try {
      System.out.println("Tentative de calcul de marge: " + savedWithoutPrice.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Erreur attendue: " + e.getMessage());
    }
  }
}
