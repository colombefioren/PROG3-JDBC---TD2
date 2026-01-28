package com.jdbctd2.model.converter;

import com.jdbctd2.model.UnitConverter;
import com.jdbctd2.model.enums.UnitType;

public class BeurreConverter implements UnitConverter {

  @Override
  public double convertToKG(double quantity, UnitType unit) {
    return switch (unit) {
      case KG -> quantity;
      case PCS -> quantity / 4.0;
      case L -> quantity / 5.0;
    };
  }

  @Override
  public boolean supportsUnit(UnitType unit) {
    return true;
  }
}
