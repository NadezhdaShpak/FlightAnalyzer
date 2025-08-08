package com.shpak;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    public static final String FILE_PATH = "src/main/resources/tickets.json";
    public Map<String, Integer> minFlightTime = new HashMap<>();
    public List<Integer> prices = new ArrayList<>();

    public static void main(String[] args) {
        Main m = new Main();
        m.readFile();
        m.printMap();
        m.avgVsMedian();
    }

    private void avgVsMedian() {
        double avg = prices.stream()
                .mapToDouble(a -> a)
                .average()
                .orElse(0.0);
        double median = median();
        System.out.printf("Разница между средней ценой и медианой составляет %1$,.2f р", Math.abs(avg - median));
    }

    private double median() {
        Collections.sort(prices);
        int size = prices.size();
        if (size % 2 == 0)
            return (prices.get(size / 2 - 1) + prices.get(size / 2) )/ 2.0;
        else return prices.get(size / 2);
    }

    private void printMap() {
        System.out.println("=".repeat(30));
        System.out.println("Минимальное время полета между\n" +
                "городами Владивосток и Тель-Авив:");
        minFlightTime.forEach((key, value) -> System.out.println(key + " " + value));
        System.out.println("=".repeat(30));
    }

    private void readFile() {
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader(FILE_PATH));
            JSONArray tickets = (JSONArray) obj;

            for (Object o : tickets) {
                JSONObject ticket = (JSONObject) o;
                String departure = (String) ticket.get("departure_airport");
                String arrival = (String) ticket.get("arrival_airport");

                if ("VVO".equalsIgnoreCase(departure) && "TLV".equalsIgnoreCase(arrival)) {
                    filterByRoute(ticket);
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void filterByRoute(JSONObject ticket) {
            String carrier = (String) ticket.get("carrier");
            Object timeObj = ticket.get("flight_time_minutes");
            Object priceObj = ticket.get("price");

            int flightTime = ((Long) timeObj).intValue();
            int price = ((Long) priceObj).intValue();

            minFlightTime.put(carrier, Math.min(minFlightTime.getOrDefault(carrier, Integer.MAX_VALUE), flightTime));
            prices.add(price);
    }
}