package com.jdbctd2;

import com.jdbctd2.model.*;
import com.jdbctd2.repository.DataRetriever;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();

    System.out.println("\n=== create order ===");

    try {
      Order order = new Order();
      order.setReference("ORD104");

      TableOrder tableOrder = new TableOrder();
      Table table = dataRetriever.findById(4);
      tableOrder.setTable(table);
      tableOrder.setArrivalDatetime(Instant.now());
      tableOrder.setDepartureDatetime(null);

      order.setTableOrder(tableOrder);

      DishOrder dishOrder = new DishOrder();
      dishOrder.setDish(dataRetriever.findDishById(1));
      dishOrder.setQuantity(1);
      order.setDishOrders(new ArrayList<>(List.of(dishOrder)));
      dataRetriever.saveOrder(order);
    } catch (RuntimeException e) {
      System.out.println(e.getMessage());
    }
  }
}
