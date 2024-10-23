package ca.jrvs.apps.jdbc.STOCKQUOTE;
import okhttp3.OkHttpClient;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting application");

        Map<String, String> properties = loadProperties("src/main/resources/properties.txt");
        if (properties == null) {
            logger.error("Failed to load properties. Exiting application.");
            return;
        }

        if (!loadDatabaseDriver(properties.get("db-class"))) {
            logger.error("Failed to load database driver. Exiting application.");
            return;
        }

        try (Connection connection = createDatabaseConnection(properties)) {
            if (connection == null) {
                logger.error("Failed to establish database connection. Exiting application.");
                return;
            }

            QuoteDao quoteDao = new QuoteDao(connection);
            PositionDao positionDao = new PositionDao(connection);
            QuoteHttpHelper quoteHttpHelper = new QuoteHttpHelper();
            QuoteService quoteService = new QuoteService(quoteDao, quoteHttpHelper);
            PositionService positionService = new PositionService(positionDao, quoteService);

            logger.info("Starting the StockQuoteController UI");
            StockQuoteController controller = new StockQuoteController(quoteService, positionService);
            controller.initClient();

        } catch (SQLException e) {
            logger.error("Error during database operations", e);
        }
    }

    private static Map<String, String> loadProperties(String filePath) {
        Map<String, String> properties = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(":");
                if (tokens.length == 2) {
                    properties.put(tokens[0].trim(), tokens[1].trim());
                } else {
                    logger.warn("Ignoring malformed property line: " + line);
                }
            }
            logger.info("Properties loaded successfully");
        } catch (IOException e) {
            logger.error("Error reading properties file", e);
            return null;
        }
        return properties;
    }

    private static boolean loadDatabaseDriver(String driverClassName) {
        try {
            logger.info("Loading database driver: " + driverClassName);
            Class.forName(driverClassName);
            logger.info("Database driver loaded successfully");
            return true;
        } catch (ClassNotFoundException e) {
            logger.error("Error loading database driver: " + driverClassName, e);
            return false;
        }
    }

    private static Connection createDatabaseConnection(Map<String, String> properties) {
        String url = String.format("jdbc:postgresql://%s:%s/%s",
                properties.get("server"), properties.get("port"), properties.get("database"));
        try {
            logger.info("Connecting to the database at " + url);
            Connection connection = DriverManager.getConnection(url, properties.get("username"), properties.get("password"));
            logger.info("Database connection established successfully");
            return connection;
        } catch (SQLException e) {
            logger.error("Error connecting to the database", e);
            return null;
        }
    }
}

