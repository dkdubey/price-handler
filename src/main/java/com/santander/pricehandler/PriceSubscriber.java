package com.santander.pricehandler;

import com.santander.pricehandler.model.Price;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class PriceSubscriber {
    private Map<String, Price> priceMap;

    public PriceSubscriber() {
        this.priceMap = new HashMap<>();
    }

    public void onMessage(String message) {
        String[] fields = message.split(",");
        if (fields.length != 5) {
            System.err.println("Invalid message format: " + message);
            return;
        }

        try {
            int id = Integer.parseInt(fields[0].trim());
            String instrument = fields[1].trim();
            double bid = Double.parseDouble(fields[2].trim());
            double ask = Double.parseDouble(fields[3].trim());
            LocalDateTime timestamp = LocalDateTime.parse(fields[4].trim());

            Price price = new Price(id, instrument, bid, ask, timestamp);
            price.adjustByMargin(-0.001, 0.001); // Apply margin adjustment

            priceMap.put(instrument, price);

            // Publish the adjusted price to a REST endpoint (implementation not provided)
            publishPrice(price);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing message: " + message);
        }
    }

    public Price getLatestPrice(String instrument) {
        return priceMap.get(instrument);
    }

    private void publishPrice(Price price) {
        // REST endpoint implementation goes here
        System.out.println("Published price: " + price);
    }

    public void writePricesToCSV(String filePath) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(filePath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("ID", "Instrument", "Bid", "Ask", "Timestamp"))) {

            for (Price price : priceMap.values()) {
                csvPrinter.printRecord(price.getId(), price.getInstrument(), price.getBid(), price.getAsk(), price.getTimestamp());
            }
            csvPrinter.flush();
        } catch (IOException e) {
            System.err.println("Error writing prices to CSV: " + e.getMessage());
        }
    }

    public void readPricesFromCSV(String filePath) {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                int id = Integer.parseInt(record.get("ID").trim());
                String instrument = record.get("Instrument").trim();
                double bid = Double.parseDouble(record.get("Bid").trim());
                double ask = Double.parseDouble(record.get("Ask").trim());
                LocalDateTime timestamp = LocalDateTime.parse(record.get("Timestamp").trim());

                Price price = new Price(id, instrument, bid, ask, timestamp);
                priceMap.put(instrument, price);
            }
        } catch (IOException e) {
            System.err.println("Error reading prices from CSV: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        PriceSubscriber subscriber = new PriceSubscriber();
        subscriber.onMessage("106, EUR/USD, 1.1000,1.2000,01-06-2020 12:01:01:001");
        subscriber.onMessage("107, EUR/JPY, 119.60,119.90,01-06-2020 12:01:02:002");
        subscriber.onMessage("108, GBP/USD, 1.2500,1.2560,01-06-2020 12:01:02:002");
        subscriber.onMessage("109, GBP/USD, 1.2499,1.2561,01-06-2020 12:01:02:100");
        subscriber.onMessage("110, EUR/JPY, 119.61,119.91,01-06-2020 12:01:02:110");

        subscriber.writePricesToCSV("prices.csv");

        // Clear the price map
        subscriber = new PriceSubscriber();

        subscriber.readPricesFromCSV("prices.csv");

        Price latestPrice = subscriber.getLatestPrice("GBP/USD");
        if (latestPrice != null) {
            System.out.println("Latest GBP/USD Price: " + latestPrice);
        } else {
            System.out.println("No price found for GBP/USD");
        }
    }
}