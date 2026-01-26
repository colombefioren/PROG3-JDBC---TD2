package com.revisionfour;

import com.revisionfour.model.CategoryEnum;
import com.revisionfour.model.Dish;
import com.revisionfour.model.DishTypeEnum;
import com.revisionfour.model.Ingredient;
import com.revisionfour.repository.DataRetriever;
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
    Ingredient fromage = new Ingredient("Fromage", CategoryEnum.DAIRY, 1200.0);
    Ingredient oignon = new Ingredient("Oignon", CategoryEnum.VEGETABLE, 500.0);
    try {
      List<Ingredient> createdIngredientsI =
          dataRetriever.createIngredients(new ArrayList<>(Arrays.asList(fromage, oignon)));
      System.out.println("createdIngredients : " + createdIngredientsI);
    } catch (RuntimeException e) {
      System.out.println("Error while creating ingredients : " + e);
    }

    // j) List<Ingredient> createIngredient(...) - carotte and laitue
    System.out.println("\n===> List<Ingredient> createIngredient(...) | carotte and laitue <===");
    Ingredient carotte = new Ingredient("Carotte", CategoryEnum.VEGETABLE, 2000.0);
    Ingredient laitue = new Ingredient(1, "Laitue", CategoryEnum.VEGETABLE, 2000.0);
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

    // test after price attribute added in Dish entity

    // initialize data before the new tests
    dataRetriever.initializeDB();

    System.out.println(
        "\n===> Test with findDishById after price attribute added in Dish entity <===");
    Dish dishT1 = dataRetriever.findDishById(1);
    System.out.println("Dish name : " + dishT1.getName());
    System.out.println("Dish price : " + dishT1.getPrice());
    System.out.println("Dish cost (ingredients) : " + dishT1.getDishCost());
    System.out.println("Dish Gross Margin : " + dishT1.getGrossMargin());

    System.out.println();

    Dish dishT2 = dataRetriever.findDishById(3);
    System.out.println("Dish name : " + dishT2.getName());
    System.out.println("Dish price : " + dishT2.getPrice());
    System.out.println("Dish cost (ingredients) : " + dishT2.getDishCost());
    try {
      System.out.println("Dish Gross Margin : " + dishT2.getGrossMargin());
    } catch (RuntimeException e) {
      System.out.println(e);
    }

    System.out.println("\n===> Test with saveDish after price attribute added in Dish entity <===");
    System.out.println("\nnew Dish");
    Ingredient laitueT3 = dataRetriever.findIngredientByName("laitue");
    List<Ingredient> newDishT3Ing = new ArrayList<>(Collections.singletonList(laitueT3));
    Dish newDishT3 = new Dish("Rabbit Cabbage", DishTypeEnum.START, newDishT3Ing, 1200.00);
    Dish savedDishT3 = dataRetriever.saveDish(newDishT3);
    System.out.println("savedDish : " + savedDishT3);

    System.out.println("\nChange price");
    Dish rizDish = dataRetriever.findDishById(3);
    rizDish.setPrice(5000.00);
    Dish newRizDish = dataRetriever.saveDish(rizDish);
    System.out.println("newRizDish : " + newRizDish);
  }
}
