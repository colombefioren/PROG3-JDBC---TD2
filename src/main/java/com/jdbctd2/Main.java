package com.jdbctd2;

import com.jdbctd2.model.*;
import com.jdbctd2.repository.DataRetriever;
import java.time.Instant;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    DataRetriever dataRetriever = new DataRetriever();
    dataRetriever.initializeDB();

    System.out.println("\n=== Test de gestion des tables ===");

    try {
    Instant now = Instant.now();
    List<Integer> availableTables = dataRetriever.findAvailableTableNumbersAt(now);
    System.out.println("\nTables disponibles à " + now + ": " + availableTables);
  }catch (RuntimeException e) {
    e.printStackTrace();}
  }
}
