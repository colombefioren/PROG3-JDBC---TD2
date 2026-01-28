package com.jdbctd2.repository;

import com.jdbctd2.model.DishOrder;
import java.util.List;

public interface DishOrderRepository {
  List<DishOrder> findDishOrdersByOrderId(Integer orderId);
}
