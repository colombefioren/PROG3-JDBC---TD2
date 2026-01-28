package com.jdbctd2.util;

import com.jdbctd2.model.UnitConverter;
import com.jdbctd2.model.converter.*;

public class UnitService {

  public static UnitConverter getConverter(String ingredientName) {
    if (ingredientName == null) {
      return new DefaultConverter();
    }

    return switch (ingredientName.toLowerCase()) {
      case "tomate" -> new TomatoConverter();
      case "laitue" -> new LaitueConverter();
      case "chocolat" -> new ChocolateConverter();
      case "poulet" -> new PouletConverter();
      case "beurre" -> new BeurreConverter();
      default -> new DefaultConverter();
    };
  }
}
