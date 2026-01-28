package com.jdbctd2.model;

public class UnitService {

  public static double getIngredientInKG(DishIngredient dishIngredient) {
    switch (dishIngredient.getIngredient().getName().toLowerCase()) {
      case "tomato":
        {
          switch (dishIngredient.getUnit()) {
            case UnitType.KG -> {
              return dishIngredient.getQuantityRequired();
            }
            case UnitType.PCS -> {
              return dishIngredient.getQuantityRequired() / 10;
            }
            case UnitType.L ->
                throw new IllegalArgumentException("L is not a valid unit for tomato");
          }
        }
      case "laitue":
        {
          switch (dishIngredient.getUnit()) {
            case UnitType.PCS -> {
              return dishIngredient.getQuantityRequired() / 2;
            }
            case UnitType.KG -> {
              return dishIngredient.getQuantityRequired();
            }
            case UnitType.L ->
                throw new IllegalArgumentException("L is not a valid unit for laitue");
          }
        }
      case "chocolat":
        {
          switch (dishIngredient.getUnit()) {
            case UnitType.PCS -> {
              return dishIngredient.getQuantityRequired() / 10;
            }
            case UnitType.KG -> {
              return dishIngredient.getQuantityRequired();
            }
            case UnitType.L -> {
              return dishIngredient.getQuantityRequired() * 2.5;
            }
          }
        }
      case "poulet":
        {
          switch (dishIngredient.getUnit()) {
            case UnitType.PCS -> {
              return dishIngredient.getQuantityRequired() / 8;
            }
            case UnitType.KG -> {
              return dishIngredient.getQuantityRequired();
            }
            case UnitType.L ->
                throw new IllegalArgumentException("L is not a valid unit for laitue");
          }
        }
      case "beurre":
        {
          switch (dishIngredient.getUnit()) {
            case UnitType.PCS -> {
              return dishIngredient.getQuantityRequired() / 4;
            }
            case UnitType.KG -> {
              return dishIngredient.getQuantityRequired();
            }
            case UnitType.L -> {
              return dishIngredient.getQuantityRequired() * 5;
            }
          }
        }
    }
      return 0;
  }
}
