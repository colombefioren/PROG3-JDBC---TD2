package com.revisionfour.repository;

import com.revisionfour.model.Order;

public interface OrderRepository {
  Order saveOrder(Order orderToSave);

  Order findOrderByReference(String reference);
}
