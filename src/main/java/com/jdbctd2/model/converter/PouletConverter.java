package com.jdbctd2.model.converter;

import com.jdbctd2.model.UnitConverter;
import com.jdbctd2.model.enums.UnitType;

public class PouletConverter implements UnitConverter {

  @Override
  public double convertToKG(double quantity, UnitType unit) {
    return switch (unit) {
      case KG -> quantity;
      case PCS -> quantity / 8.0;
      case L -> throw new IllegalArgumentException("L is not a valid unit for chicken");
    };
  }

  @Override
  public boolean supportsUnit(UnitType unit) {
    return unit == UnitType.KG || unit == UnitType.PCS;
  }
}
