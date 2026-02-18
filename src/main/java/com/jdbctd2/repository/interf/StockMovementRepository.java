package com.jdbctd2.repository.interf;

import com.jdbctd2.model.StockMovement;
import com.jdbctd2.model.StockPeriodValue;
import com.jdbctd2.model.StockValue;
import java.time.Instant;
import java.util.List;

public interface StockMovementRepository {
  StockMovement findStockMovementById(Integer id);

  List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId);

  StockValue getStockValueAt(Instant t, Integer ingredientIdentifier);

  List<StockPeriodValue> getStockValues(
      String peiodicity, Instant intervalleMin, Instant intervalleMax);
}
