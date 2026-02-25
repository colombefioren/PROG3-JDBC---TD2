package com.jdbctd2;

import com.jdbctd2.model.Dish;
import com.jdbctd2.model.Ingredient;
import com.jdbctd2.model.StockPeriodValue;
import com.jdbctd2.repository.DataRetriever;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        DataRetriever dataRetriever = new DataRetriever();

        dataRetriever.initializeDB();

        System.out.println("\n===> Stock::getQuantity | POO Approach <===");
        List<Ingredient> allIngredients = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            try {
                allIngredients.add(dataRetriever.findIngredientById(i));

            } catch (RuntimeException e) {
                System.out.println("Ingredient with id " + i + " not found: " + e.getMessage());
            }
        }

        for (Ingredient ingredient : allIngredients) {
            System.out.println("\nIngredient name : " + ingredient.getName());
            System.out.println(
                    "Stock : " + ingredient.getStockValueAt(Instant.parse("2024-01-06T12:00:00Z")));
        }

        System.out.println("\n===> Stock::getQuantity | DB SIDE PROCESSING Approach <===");

        for (Ingredient ingredient : allIngredients) {
            System.out.println("\nIngredient name : " + ingredient.getName());
            System.out.println(
                    "Stock : "
                            + dataRetriever.getStockValueAt(
                            Instant.parse("2024-01-06T12:00:00Z"), ingredient.getId()));
        }

        System.out.println("\n===> Test getGrossMargin | POO Approach");
        List<Dish> allDishes = new ArrayList<>();
        for (int i = 5; i > 0; i--) {
            try {
                allDishes.add(dataRetriever.findDishById(i));
            } catch (RuntimeException e) {
                System.out.println("Dish with id " + i + " not found: " + e.getMessage());
            }
        }
        for (Dish dish : allDishes) {
            try {
                System.out.println("\nDish name : " + dish.getName());
                System.out.println("Dish price : " + dish.getPrice());
                System.out.println("Dish cost (ingredients) : " + dish.getDishCost());
                System.out.println("Gross Margin : " + dish.getGrossMargin());
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println("\n===> Test getGrossMargin | DB SIDE PROCESSING Approach");
        for (Dish dish : allDishes) {
            try {
                System.out.println("\nDish name : " + dish.getName());
                System.out.println("Dish price : " + dish.getPrice());
                System.out.println("Dish cost (ingredients) : " + dataRetriever.getDishCost(dish.getId()));
                System.out.println("Gross Margin : " + dataRetriever.getGrossMargin(dish.getId()));
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }
        }

        System.out.println("\n===> Test getStockValues (periodic) | DB SIDE PROCESSING Approach <===");

        String periodicity = "day";
        Instant intervalleMin = Instant.parse("2024-01-04T00:00:00Z");
        Instant intervalleMax = Instant.parse("2024-01-07T00:00:00Z");

        List<StockPeriodValue> stockPeriodValues =
                dataRetriever.getStockStats(periodicity, intervalleMin, intervalleMax);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        .withZone(ZoneOffset.UTC);

        Set<String> allDates = new TreeSet<>();
        Map<Long, Map<String, Double>> table = new LinkedHashMap<>();
        Map<Long, String> ingredientNames = new LinkedHashMap<>();

        for (StockPeriodValue spv : stockPeriodValues) {
            String formattedDate = formatter.format(spv.getPeriod());
            allDates.add(formattedDate);

            table.computeIfAbsent((long) spv.getIngredientId(), k -> new LinkedHashMap<>())
                    .put(formattedDate, spv.getStockValue());

            ingredientNames.putIfAbsent((long) spv.getIngredientId(), spv.getIngredientName());
        }

        System.out.printf("%-4s%-12s", "ID", "Name");
        for (String date : allDates) {
            System.out.printf("%-13s", date);
        }
        System.out.println();

        for (Long id : table.keySet()) {
            System.out.printf("%-4d%-12s", id, ingredientNames.get(id));

            Map<String, Double> stocks = table.get(id);
            for (String date : allDates) {
                Double value = stocks.getOrDefault(date, 0.0);
                System.out.printf("%-13.1f", value);
            }

            System.out.println();
        }
    }
}
