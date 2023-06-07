package com.santander.pricehandler;

import com.santander.pricehandler.model.MarginCalculator;
import com.santander.pricehandler.model.Price;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

@Service
public class PriceSubscriber {

    private static final Logger logger = Logger.getLogger(PriceSubscriber.class.getName());

    private static Map<String, Price> priceMap;
    private final static int INDEX_ID=0;
    private final static int INDEX_INSTRUMENT=1;
    private final static int INDEX_BID=2;
    private final static int INDEX_ASK=3;
    private final static int INDEX_DATE_TIME=4;
    private MarginCalculator marginCalculator;

    public PriceSubscriber() {
        this.priceMap = new ConcurrentHashMap<>();
        marginCalculator = new MarginCalculator();
    }

    public void onMessage(String message) {
        String[] fields = message.split(",");
        if (fields.length != 5) {
            logger.warning("Invalid message format: " + message);
            return;
        }

        try {
            int id = Integer.parseInt(fields[INDEX_ID].trim());
            String instrument = fields[INDEX_INSTRUMENT].trim();
            BigDecimal bid = new BigDecimal(fields[INDEX_BID].trim());
            BigDecimal ask = new BigDecimal(fields[INDEX_ASK].trim());
            LocalDateTime timestamp =null;
            try {
                LocalDateTime date = LocalDateTime.parse(fields[INDEX_DATE_TIME].trim(), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                timestamp = LocalDateTime.now();
            }

            Price price = new Price(id, instrument, bid, ask, timestamp);
            price.setAsk(marginCalculator.adjustAskByMargin(price.getAsk(), new BigDecimal(0.001)));
            price.setBid(marginCalculator.adjustBidByMargin(price.getBid(), new BigDecimal(-0.001)));

            priceMap.put(instrument, price); // Check the Hashmap Compute

            // Publish the adjusted price to a REST endpoint
            publishPrice(price);
        } catch (NumberFormatException e) {
            logger.warning("Error parsing message: " + message);
        }
    }

    public Price getLatestPrice(String instrument) {
        return priceMap.get(instrument);
    }

    private void publishPrice(Price price) {
        // REST endpoint implementation goes here
        logger.info("Published price: " + price);
    }

    public void writePricesToCSV(String filePath) {
        try (Writer writer = Files.newBufferedWriter(Paths.get(filePath));
             CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("ID", "Instrument", "Bid", "Ask", "Timestamp"))) {

            for (Price price : priceMap.values()) {
                csvPrinter.printRecord(price.getId(), price.getInstrument(), price.getBid(), price.getAsk(), price.getTimestamp());
            }
            csvPrinter.flush();
            logger.info("Prices written to CSV: " + filePath);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing prices to CSV: " + e.getMessage(), e);
        }
    }

    public void readPricesFromCSV(String filePath) {
        try (Reader reader = Files.newBufferedReader(Paths.get(filePath));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                int id = Integer.parseInt(record.get("ID").trim());
                String instrument = record.get("Instrument").trim();
                BigDecimal bid = new BigDecimal(record.get("Bid").trim());
                BigDecimal ask = new BigDecimal(record.get("Ask").trim());
                LocalDateTime timestamp =null;
                try {
                    LocalDateTime date = LocalDateTime.parse(record.get("Timestamp").trim(), DateTimeFormatter.ISO_DATE_TIME);
                } catch (DateTimeParseException e) {
                    timestamp = LocalDateTime.now();
                }

                Price price = new Price(id, instrument, bid, ask, timestamp);
                priceMap.put(instrument, price);
            }
            logger.info("Prices read from CSV: " + filePath);
        } catch (IOException e) {
            logger.log(Level.SEVERE,"Error reading prices from CSV: " + e.getMessage());
        }
    }

    @PostConstruct
    public static void readDataFromCSV() {
        PriceSubscriber subscriber = new PriceSubscriber();
        subscriber.onMessage("106, EUR-USD, 1.1000,1.2000,01-06-2020 12:01:01:001");
        subscriber.onMessage("107, EUR-JPY, 119.60,119.90,01-06-2020 12:01:02:002");
        subscriber.onMessage("108, GBP-USD, 1.2500,1.2560,01-06-2020 12:01:02:002");
        subscriber.onMessage("109, GBP-USD, 1.2499,1.2561,01-06-2020 12:01:02:100");
        subscriber.onMessage("110, EUR-JPY, 119.61,119.91,01-06-2020 12:01:02:110");

        subscriber.writePricesToCSV("prices.csv");

        // Clear the price map
        subscriber = new PriceSubscriber();

        subscriber.readPricesFromCSV("prices.csv");

        Price latestPrice = subscriber.getLatestPrice("GBP-USD");
        if (latestPrice != null) {
            System.out.println("Latest GBP-USD Price: " + latestPrice);
        } else {
            System.out.println("No price found for GBP/USD");
        }
    }

}