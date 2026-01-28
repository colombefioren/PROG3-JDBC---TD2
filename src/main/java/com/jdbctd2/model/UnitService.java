package com.jdbctd2.model;

public class UnitService {

  public static double getIngredientInKG(Ingredient ingredient, Double quantity, UnitType unit) {
    switch (ingredient.getName().toLowerCase()) {
      case "tomato":
        {
          switch (unit) {
            case UnitType.KG -> {
              return quantity;
            }
            case UnitType.PCS -> {
              return quantity / 10;
            }
            case UnitType.L ->
                throw new IllegalArgumentException("L is not a valid unit for tomato");
          }
        }
      case "laitue":
        {
          switch (unit) {
            case UnitType.PCS -> {
              return quantity / 2;
            }
            case UnitType.KG -> {
              return quantity;
            }
            case UnitType.L ->
                throw new IllegalArgumentException("L is not a valid unit for laitue");
          }
        }
      case "chocolat":
        {
          switch (unit) {
            case UnitType.PCS -> {
              return quantity / 10;
            }
            case UnitType.KG -> {
              return quantity;
            }
            case UnitType.L -> {
              return quantity * 2.5;
            }
          }
        }
      case "poulet":
        {
          switch (unit) {
            case UnitType.PCS -> {
              return quantity / 8;
            }
            case UnitType.KG -> {
              return quantity;
            }
            case UnitType.L ->
                throw new IllegalArgumentException("L is not a valid unit for laitue");
          }
        }
      case "beurre":
        {
          switch (unit) {
            case UnitType.PCS -> {
              return quantity / 4;
            }
            case UnitType.KG -> {
              return quantity;
            }
            case UnitType.L -> {
              return quantity * 5;
            }
          }
        }
    }
    return 0;
  }
}
