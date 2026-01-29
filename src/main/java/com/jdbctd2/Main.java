package com.jdbctd2;

import com.jdbctd2.model.*;
import com.jdbctd2.repository.DataRetriever;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();
    dataRetriever.initializeDB();

    System.out.println("\n=== Test de gestion des tables ===");

    try {
      Table table1 = dataRetriever.findByNumber(1);
      if (table1 != null) {
        TableOrder tableOrder = new TableOrder(table1, Instant.now(), null);
        List<DishOrder> dishOrders = new ArrayList<>();
        Dish dish1 = dataRetriever.findDishById(1);
        dishOrders.add(new DishOrder(dish1, 2));

        Order order = new Order("TEST-TABLE-001", Instant.now(), dishOrders, tableOrder);

        Order savedOrder = dataRetriever.saveOrder(order);
        System.out.println(
            "Commande créée: " + savedOrder.getReference() + " avec table " + table1.getNumber());
      }
    } catch (RuntimeException e) {
      System.out.println("Erreur: " + e.getMessage());
    }

    Instant now = Instant.now();
    List<Integer> availableTables = dataRetriever.findAvailableTableNumbersAt(now);
    System.out.println("\nTables disponibles à " + now + ": " + availableTables);
  }
}
