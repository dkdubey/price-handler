package com.santander.pricehandler;

import com.santander.pricehandler.model.Price;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PriceSubscriberTest {
    private PriceSubscriber subscriber;
    private Path tempFilePath;

    @BeforeEach
    public void setup() throws IOException {
        subscriber = new PriceSubscriber();
        tempFilePath = Files.createTempFile("prices", ".csv");
    }

    @Test
    public void testOnMessage_ValidMessage() {
        String message = "106, EUR/USD, 1.1000,1.2000,01-06-2020 12:01:01:001";
        subscriber.onMessage(message);

        Price price = subscriber.getLatestPrice("EUR/USD");
        Assertions.assertNotNull(price);
        Assertions.assertEquals(1.09989, price.getBid(), 0.00001);
        Assertions.assertEquals(1.20012, price.getAsk(), 0.00001);
    }

    @Test
    public void testOnMessage_InvalidMessage() {
        String message = "Invalid message format";
        subscriber.onMessage(message);

        Price price = subscriber.getLatestPrice("EUR/USD");
        Assertions.assertNull(price);
    }

    @Test
    public void testGetLatestPrice_PriceNotFound() {
        Price price = subscriber.getLatestPrice("GBP/USD");
        Assertions.assertNull(price);
    }

    @Test
    public void testReadPricesFromCSV() {
        String csvContent = "ID,Instrument,Bid,Ask,Timestamp\n" +
                "101,EUR/USD,1.2000,1.3000,01-06-2020 12:01:01:001\n" +
                "102,EUR/JPY,119.60,119.90,01-06-2020 12:01:02:002\n" +
                "103,GBP/USD,1.2500,1.2560,01-06-2020 12:01:02:002\n" +
                "104,GBP/USD,1.2499,1.2561,01-06-2020 12:01:02:100\n" +
                "105,EUR/JPY,119.61,119.91,01-06-2020 12:01:02:110\n";
        writeContentToFile(csvContent);

        subscriber.readPricesFromCSV(tempFilePath.toString());

        Price price = subscriber.getLatestPrice("GBP/USD");
        Assertions.assertNotNull(price);
        Assertions.assertEquals(1.24989, price.getBid(), 0.00001);
        Assertions.assertEquals(1.25612, price.getAsk(), 0.00001);
    }

    @Test
    public void testWritePricesToCSV() {
        subscriber.onMessage("101, EUR/USD, 1.2000,1.3000,01-06-2020 12:01:01:001");
        subscriber.onMessage("102, EUR/JPY, 119.60,119.90,01-06-2020 12:01:02:002");
        subscriber.onMessage("103, GBP/USD, 1.2500,1.2560,01-06-2020 12:01:02:002");
        subscriber.onMessage("104, GBP/USD, 1.2499,1.2561,01-06-2020 12:01:02:100");
        subscriber.onMessage("105, EUR/JPY, 119.61,119.91,01-06-2020 12:01:02:110");

        subscriber.writePricesToCSV(tempFilePath.toString());

        List<String> lines = readLinesFromFile();
        Assertions.assertEquals(6, lines.size()); // Including the header

        String header = lines.get(0);
        Assertions.assertEquals("ID,Instrument,Bid,Ask,Timestamp", header);

        String lastLine = lines.get(lines.size() - 1);
        Assertions.assertTrue(lastLine.contains("105,EUR/JPY,119.61,119.91,01-06-2020 12:01:02:110"));
    }

    private void writeContentToFile(String content) {
        try {
            Files.write(tempFilePath, content.getBytes());
        } catch (IOException e) {
            System.err.println("Error writing content to file: " + e.getMessage());
        }
    }

    private List<String> readLinesFromFile() {
        try {
            return Files.readAllLines(tempFilePath);
        } catch (IOException e) {
            System.err.println("Error reading lines from file: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}
