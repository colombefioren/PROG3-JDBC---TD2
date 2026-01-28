package com.jdbctd2.model;

import com.jdbctd2.model.enums.UnitType;

public interface UnitConverter {
  double convertToKG(double quantity, UnitType unit);

  boolean supportsUnit(UnitType unit);
}
