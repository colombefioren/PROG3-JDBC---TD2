package com.revisionfour.repository;

import com.revisionfour.model.StockMovement;
import java.util.List;

public interface StockMovementRepository {
  StockMovement findStockMovementById(Integer id);

  List<StockMovement> findStockMovementsByIngredientId(Integer ingredientId);
}
