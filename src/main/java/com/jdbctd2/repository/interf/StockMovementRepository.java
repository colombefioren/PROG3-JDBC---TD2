package com.jdbctd2.repository.interf;

import com.jdbctd2.model.StockMovement;
import java.util.List;

public interface StockMovementRepository {
  StockMovement findStockMovementById(Integer id);

  List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId);
}
