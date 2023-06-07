package com.santander.pricehandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.logging.*;

@SpringBootApplication
public class PriceHandlerApplication {

	private static final Logger logger = Logger.getLogger(PriceHandlerApplication.class.getName());

	private static void configureLogger() {
		LogManager.getLogManager().reset();
		Logger rootLogger = LogManager.getLogManager().getLogger("");
		rootLogger.setLevel(Level.INFO);

		ConsoleHandler consoleHandler = new ConsoleHandler();
		consoleHandler.setLevel(Level.INFO);
		rootLogger.addHandler(consoleHandler);

		try {
			FileHandler fileHandler = new FileHandler("application.log");
			fileHandler.setLevel(Level.ALL);
			fileHandler.setFormatter(new SimpleFormatter());
			rootLogger.addHandler(fileHandler);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error configuring logger: " + e.getMessage(), e);
		}
	}

	public static void main(String[] args) {
		configureLogger();
		SpringApplication.run(PriceHandlerApplication.class, args);
	}

}
