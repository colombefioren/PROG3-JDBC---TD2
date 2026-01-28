package com.revisionfour.repository;

import com.revisionfour.model.DishOrder;
import java.util.List;

public interface DishOrderRepository {
  List<DishOrder> findDishOrdersByOrderId(Integer orderId);
}
