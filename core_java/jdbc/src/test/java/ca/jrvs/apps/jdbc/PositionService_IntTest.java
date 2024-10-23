package ca.jrvs.apps.jdbc;



import ca.jrvs.apps.jdbc.STOCKQUOTE.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.junit.Assert.*;

public class PositionService_IntTest {

    private PositionService positionService;
    private PositionDao positionDao;
    private QuoteService quoteService;
    private Connection connection;

    @Before
    public void setUp() throws Exception {
        // Establish a connection to the test database
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/stock_quote_test", "postgres", "password");
        positionDao = new PositionDao(connection);
        QuoteDao quoteDao = new QuoteDao(connection);
        QuoteHttpHelper httpHelper = new QuoteHttpHelper(mockClient);
        quoteService = new QuoteService(quoteDao, httpHelper);
        positionService = new PositionService(positionDao, quoteService);

        // Clean up any existing data to ensure a clean test environment
        positionDao.deleteAll();
        quoteDao.deleteById("TSLA");

        // Add a quote for TSLA to satisfy foreign key constraints
        Quote tslaQuote = new Quote();
        tslaQuote.setTicker("TSLA");
        tslaQuote.setOpen(700.0);
        tslaQuote.setHigh(750.0);
        tslaQuote.setLow(680.0);
        tslaQuote.setPrice(720.0);
        tslaQuote.setVolume(10000);
        tslaQuote.setLatestTradingDay(new java.sql.Date(System.currentTimeMillis()));
        tslaQuote.setPreviousClose(710.0);
        tslaQuote.setChange(10.0);
        tslaQuote.setChangePercent("1.41%");

        quoteDao.save(tslaQuote);
    }

    @After
    public void tearDown() throws Exception {
        // Clean up the database after tests to maintain test isolation
        positionDao.deleteAll();
        QuoteDao quoteDao = new QuoteDao(connection);
        quoteDao.deleteById("TSLA");
        connection.close();
    }

    @Test
    public void testBuyPosition() {
        // Test buying a position
        Position position = positionService.buy("TSLA", 50, 700.0);
        assertNotNull("Position should not be null", position);
        assertEquals("Ticker should be TSLA", "TSLA", position.getTicker());
        assertEquals("Number of shares should be 50", 50, position.getNumOfShares());
        assertEquals("Value paid should be 35000.0", 35000.0, position.getValuePaid(), 0);
    }

    @Test
    public void testSellPosition() {
        // First, buy some shares
        positionService.buy("TSLA", 50, 700.0);

        // Then, sell all the shares
        positionService.sell("TSLA");

        // Verify that the position is removed
        Optional<Position> position = positionDao.findById("TSLA");
        assertFalse("Position should be removed after selling", position.isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyWithInvalidTicker() {
        // Attempt to buy a stock with an invalid ticker should throw an exception
        positionService.buy("INVALID", 10, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSellNonExistentPosition() {
        // Attempt to sell a position that doesn't exist should throw an exception
        positionService.sell("TSLA");
    }

    @Test
    public void testMultipleBuyAndSellOperations() {
        // Buy multiple times
        positionService.buy("TSLA", 30, 700.0);
        positionService.buy("TSLA", 20, 710.0);

        // Check the aggregated position
        Optional<Position> position = positionDao.findById("TSLA");
        assertTrue("Position should exist", position.isPresent());
        assertEquals("Total shares should be 50", 50, position.get().getNumOfShares());
        assertEquals("Total value paid should be 35500.0", 35500.0, position.get().getValuePaid(), 0);

        // Sell all shares
        positionService.sell("TSLA");

        // Verify that the position is removed
        position = positionDao.findById("TSLA");
        assertFalse("Position should be removed after selling", position.isPresent());
    }
}

