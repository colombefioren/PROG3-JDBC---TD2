package com.jdbctd2.repository;

import com.jdbctd2.model.Order;

public interface OrderRepository {
  Order saveOrder(Order orderToSave);

  Order findOrderByReference(String reference);
}
