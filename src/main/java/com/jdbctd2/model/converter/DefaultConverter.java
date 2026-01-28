package com.jdbctd2.model.converter;

import com.jdbctd2.model.UnitConverter;
import com.jdbctd2.model.enums.UnitType;

public class DefaultConverter implements UnitConverter {

  @Override
  public double convertToKG(double quantity, UnitType unit) {
    return switch (unit) {
      case KG -> quantity;
      default -> throw new IllegalArgumentException("Unit not supported for this ingredient");
    };
  }

  @Override
  public boolean supportsUnit(UnitType unit) {
    return unit == UnitType.KG;
  }
}
