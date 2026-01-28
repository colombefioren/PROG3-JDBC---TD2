package com.jdbctd2.repository.interf;

import com.jdbctd2.model.Order;

public interface OrderRepository {
  Order saveOrder(Order orderToSave);

  Order findOrderByReference(String reference);

  Order findOrderById(Integer id);
}
