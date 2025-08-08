package com.shpak;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    private static final String FILE_PATH = "src/main/resources/tickets.json";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
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
            return (prices.get(size / 2 - 1) + prices.get(size / 2)) / 2.0;
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
            JSONObject jsonObject = (JSONObject) obj;
            JSONArray tickets = (JSONArray) jsonObject.get("tickets");

            for (Object o : tickets) {
                JSONObject ticket = (JSONObject) o;
                String departure = (String) ticket.get("origin");
                String arrival = (String) ticket.get("destination");

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
        String departureDateStr = (String) ticket.get("departure_date");
        String departureTimeStr = (String) ticket.get("departure_time");
        String arrivalDateStr = (String) ticket.get("arrival_date");
        String arrivalTimeStr = (String) ticket.get("arrival_time");

        LocalDateTime departureDateTime = getLocalDateTime(departureDateStr, departureTimeStr);
        LocalDateTime arrivalDateTime = getLocalDateTime(arrivalDateStr, arrivalTimeStr);

        long flightTimeMinutes = Duration.between(departureDateTime, arrivalDateTime).toMinutes();

        Object priceObj = ticket.get("price");
        int price = ((Long) priceObj).intValue();

        minFlightTime.put(carrier, (int)Math.min(minFlightTime.getOrDefault(carrier, Integer.MAX_VALUE), flightTimeMinutes));
        prices.add(price);
    }

    private static LocalDateTime getLocalDateTime(String departureDateStr, String departureTimeStr) {
        LocalDate departureDate = LocalDate.parse(departureDateStr, DATE_FORMAT);
        LocalTime departureTime = LocalTime.parse(departureTimeStr, TIME_FORMAT);
        return LocalDateTime.of(departureDate, departureTime);
    }
}