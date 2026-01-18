package com.jdbctd2;

import com.jdbctd2.model.*;
import com.jdbctd2.model.UnitEnum;
import com.jdbctd2.repository.DataRetriever;
import java.util.ArrayList;
import java.util.Arrays;
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
    System.out.println("page=3,size=5 : " + ingredientListD);

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
    Ingredient laitue = new Ingredient(1, "Laitue", 2000.0, CategoryEnum.VEGETABLE);
    try {
      List<Ingredient> createdIngredientsJ =
          dataRetriever.createIngredients(new ArrayList<>(Arrays.asList(carotte, laitue)));
      System.out.println("createdIngredients : " + createdIngredientsJ);
    } catch (RuntimeException e) {
      System.out.println("Error while creating ingredients : " + e);
    }

    // k) Dish saveDish(...) - soupe de légumes
    System.out.println("\n===> Dish saveDish(...) | name=Soupe de légumes dishType=START <===");
    Ingredient oignonIngredient = dataRetriever.findIngredientByName("oignon");
    Dish newDishK = new Dish("Soupe de légumes", DishTypeEnum.START, (Double) null);

    if (oignonIngredient != null) {
      List<DishIngredient> dishIngredients = new ArrayList<>();
      DishIngredient di = new DishIngredient();
      di.setIngredient(oignonIngredient);
      di.setQuantityRequired(0.5);
      di.setUnit(UnitEnum.KG);
      dishIngredients.add(di);
      newDishK.setDishIngredients(dishIngredients);
    }

    Dish savedDishK = dataRetriever.saveDish(newDishK);
    System.out.println("savedDish : " + savedDishK);

    // l) Dish saveDish(...) - salade fraîche
    System.out.println("\n===> Dish saveDish(...) | id=1 name=Salade fraîche dishType=START <===");
    Ingredient laitueIngredient = dataRetriever.findIngredientByName("laitue");
    Ingredient fromageIngredient = dataRetriever.findIngredientByName("fromage");
    Ingredient tomateIngredient = dataRetriever.findIngredientByName("tomate");

    Dish dishL = dataRetriever.findDishById(1);
    dishL.setName("Salade fraîche");
    dishL.setDishType(DishTypeEnum.START);

    List<DishIngredient> dishIngredientsL = new ArrayList<>();

    if (oignonIngredient != null) {
      DishIngredient di1 = new DishIngredient();
      di1.setIngredient(oignonIngredient);
      di1.setQuantityRequired(0.1);
      di1.setUnit(UnitEnum.KG);
      dishIngredientsL.add(di1);
    }

    if (laitueIngredient != null) {
      DishIngredient di2 = new DishIngredient();
      di2.setIngredient(laitueIngredient);
      di2.setQuantityRequired(0.2);
      di2.setUnit(UnitEnum.KG);
      dishIngredientsL.add(di2);
    }

    if (tomateIngredient != null) {
      DishIngredient di3 = new DishIngredient();
      di3.setIngredient(tomateIngredient);
      di3.setQuantityRequired(0.15);
      di3.setUnit(UnitEnum.KG);
      dishIngredientsL.add(di3);
    }

    if (fromageIngredient != null) {
      DishIngredient di4 = new DishIngredient();
      di4.setIngredient(fromageIngredient);
      di4.setQuantityRequired(0.05);
      di4.setUnit(UnitEnum.KG);
      dishIngredientsL.add(di4);
    }

    dishL.setDishIngredients(dishIngredientsL);
    Dish savedDishL = dataRetriever.saveDish(dishL);
    System.out.println("savedDish : " + savedDishL);

    // m) Dish saveDish(...) - Salade de fromage
    System.out.println(
        "\n===> Dish saveDish(...) | id=1 name=Salade de fromage dishType=START <===");
    Dish dishM = dataRetriever.findDishById(1);
    dishM.setName("Salade de fromage");
    dishM.setDishType(DishTypeEnum.START);

    List<DishIngredient> dishIngredientsM = new ArrayList<>();
    if (fromageIngredient != null) {
      DishIngredient di = new DishIngredient();
      di.setIngredient(fromageIngredient);
      di.setQuantityRequired(0.3);
      di.setUnit(UnitEnum.KG);
      dishIngredientsM.add(di);
    }

    dishM.setDishIngredients(dishIngredientsM);
    Dish savedDishM = dataRetriever.saveDish(dishM);
    System.out.println("savedDish : " + savedDishM);

    System.out.println("\n===> findDishById avec getGrossMargin <===");

    // dish of id=1 and price=3500
    try {
      Dish dish1 = dataRetriever.findDishById(1);
      System.out.println("Dish: " + dish1.getName());
      System.out.println("Price: " + dish1.getSellingPrice());
      System.out.println("Ingredient cost: " + dish1.getDishCost());
      System.out.println("Gross Margin: " + dish1.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }

    // savedish and update price of id=3 to be 3500
    System.out.println("\n===> saveDish update the dish of id 3 price to 3500 <===");

    Dish dishToUpdate = dataRetriever.findDishById(3);
    System.out.println(dishToUpdate);
    System.out.println("Ancient dish price: " + dishToUpdate.getSellingPrice());
    try {
      System.out.println("Gross Margin before update: " + dishToUpdate.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }
    dishToUpdate.setSellingPrice(3500.0);
    Dish updatedDish = dataRetriever.saveDish(dishToUpdate);
    System.out.println("New dish price: " + updatedDish.getSellingPrice());

    try {
      System.out.println("Gross Margin after update: " + updatedDish.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }

    System.out.println("\n===> saveDish create a new dish with price <===");
    laitueIngredient = dataRetriever.findIngredientByName("laitue");
    tomateIngredient = dataRetriever.findIngredientByName("tomate");

    Dish newDish = new Dish("Salade César", DishTypeEnum.START, 2500.0);

    List<DishIngredient> dishIngredientsNew = new ArrayList<>();

    if (laitueIngredient != null) {
      DishIngredient di1 = new DishIngredient();
      di1.setIngredient(laitueIngredient);
      di1.setQuantityRequired(0.3);
      di1.setUnit(UnitEnum.KG);
      dishIngredientsNew.add(di1);
    }

    if (tomateIngredient != null) {
      DishIngredient di2 = new DishIngredient();
      di2.setIngredient(tomateIngredient);
      di2.setQuantityRequired(0.25);
      di2.setUnit(UnitEnum.KG);
      dishIngredientsNew.add(di2);
    }

    newDish.setDishIngredients(dishIngredientsNew);
    Dish savedNewDish = dataRetriever.saveDish(newDish);
    System.out.println(savedNewDish);

    try {
      System.out.println("Gross Margin of the new dish: " + savedNewDish.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }

    System.out.println("\n===> saveDish Dish without price <===");
    Dish dishWithoutPrice = new Dish("Soupe du jour", DishTypeEnum.START, (Double) null);

    List<DishIngredient> dishIngredientsWithout = new ArrayList<>();

    if (oignonIngredient != null) {
      DishIngredient di = new DishIngredient();
      di.setIngredient(oignonIngredient);
      di.setQuantityRequired(0.2);
      di.setUnit(UnitEnum.KG);
      dishIngredientsWithout.add(di);
    }

    dishWithoutPrice.setDishIngredients(dishIngredientsWithout);
    Dish savedWithoutPrice = dataRetriever.saveDish(dishWithoutPrice);
    System.out.println("Dish: " + savedWithoutPrice.getName());
    System.out.println("Price: " + savedWithoutPrice.getSellingPrice());

    try {
      System.out.println("Gross Margin: " + savedWithoutPrice.getGrossMargin());
    } catch (IllegalStateException e) {
      System.out.println("Error: " + e.getMessage());
    }

    // initialize the db before the new tests
    dataRetriever.initializeDB();

    System.out.println("\n===> New test to verify costs with quantities <===\n");

    for (int i = 1; i <= 5; i++) {
      Dish dish = dataRetriever.findDishById(i);
      System.out.println("Dish " + i + ": " + dish.getName());
      System.out.println("  Calculated Price: " + dish.getDishCost());
      System.out.println("  Selling Price: " + dish.getSellingPrice());
      try {
        System.out.println("  Gross Margin: " + dish.getGrossMargin());
      } catch (IllegalStateException e) {
        System.out.println("  Exception: " + e.getMessage());
      }
      System.out.println("  Ingredients: " + dish.getDishIngredients().size());
      for (DishIngredient di : dish.getDishIngredients()) {
        System.out.println(
            "    - "
                + di.getIngredient().getName()
                + ": "
                + di.getQuantityRequired()
                + " "
                + di.getUnit()
                + " (cost: "
                + di.getCost()
                + ")");
      }
      System.out.println();
    }
    // initialize the db at the end as well because why not
    dataRetriever.initializeDB();
  }
}
