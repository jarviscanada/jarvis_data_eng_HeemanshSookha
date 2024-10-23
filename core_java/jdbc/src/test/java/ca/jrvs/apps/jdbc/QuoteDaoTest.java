package ca.jrvs.apps.jdbc;



import ca.jrvs.apps.jdbc.STOCKQUOTE.Quote;
import ca.jrvs.apps.jdbc.STOCKQUOTE.QuoteDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.Assert.*;

public class QuoteDaoTest {

    private Connection connection;
    private QuoteDao quoteDao;

    @Before
    public void setUp() throws Exception {
        // Establish a connection to the test database
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/stock_quote_test", "postgres", "password");
        quoteDao = new QuoteDao(connection);

        // Clean up any existing data to ensure a clean test environment
        quoteDao.deleteAll();
    }

    @After
    public void tearDown() throws SQLException {
        // Clean up the database after tests to maintain test isolation
        quoteDao.deleteAll();
        connection.close();
    }

    @Test
    public void testSaveAndFindQuote() {
        // Create a new quote
        Quote quote = createTestQuote("GOOGL", 2800.0, 2850.0, 2750.0, 2820.0, 10000, 2790.0, 30.0, "1.08%");

        // Save the quote in the database
        quoteDao.save(quote);

        // Find the saved quote by ticker
        Optional<Quote> result = quoteDao.findById("GOOGL");
        assertTrue("Quote should be present", result.isPresent());
        assertEquals("GOOGL", result.get().getTicker());
        assertEquals(2820.0, result.get().getPrice(), 0);
    }

    @Test
    public void testUpdateQuote() {
        // Create and save a new quote
        Quote quote = createTestQuote("AMZN", 3300.0, 3350.0, 3250.0, 3320.0, 8000, 3290.0, 30.0, "0.91%");
        quoteDao.save(quote);

        // Update the quote
        quote.setPrice(3330.0);
        quote.setVolume(9000);
        quoteDao.save(quote);

        // Verify the updated quote
        Optional<Quote> updatedQuote = quoteDao.findById("AMZN");
        assertTrue("Updated quote should be present", updatedQuote.isPresent());
        assertEquals(3330.0, updatedQuote.get().getPrice(), 0);
        assertEquals(9000, updatedQuote.get().getVolume());
    }

    @Test
    public void testFindAllQuotes() {
        // Add multiple quotes
        quoteDao.save(createTestQuote("FB", 250.0, 260.0, 240.0, 255.0, 6000, 245.0, 10.0, "4.08%"));
        quoteDao.save(createTestQuote("NFLX", 500.0, 510.0, 490.0, 505.0, 4000, 495.0, 10.0, "2.02%"));

        // Retrieve all quotes
        Iterable<Quote> quotes = quoteDao.findAll();
        assertNotNull("The list of quotes should not be null", quotes);

        int count = 0;
        for (Quote q : quotes) {
            count++;
        }
        assertEquals("There should be 2 quotes", 2, count);
    }

    @Test
    public void testDeleteQuoteById() {
        // Create and save a new quote
        Quote quote = createTestQuote("MSFT", 200.0, 210.0, 190.0, 205.0, 7000, 195.0, 10.0, "5.13%");
        quoteDao.save(quote);

        // Delete the quote by ticker
        quoteDao.deleteById("MSFT");

        // Verify the quote is deleted
        Optional<Quote> result = quoteDao.findById("MSFT");
        assertFalse("Quote should not be present after deletion", result.isPresent());
    }

    @Test
    public void testDeleteAllQuotes() {
        // Add multiple quotes
        quoteDao.save(createTestQuote("TSLA", 600.0, 650.0, 580.0, 640.0, 5000, 620.0, 20.0, "3.23%"));
        quoteDao.save(createTestQuote("AAPL", 120.0, 130.0, 110.0, 125.0, 8000, 115.0, 10.0, "8.70%"));

        // Delete all quotes
        quoteDao.deleteAll();

        // Verify no quotes remain
        Iterable<Quote> quotes = quoteDao.findAll();
        assertFalse("No quotes should be present after deletion", quotes.iterator().hasNext());
    }

    private Quote createTestQuote(String ticker, double open, double high, double low, double price, int volume, double previousClose, double change, String changePercent) {
        Quote quote = new Quote();
        quote.setTicker(ticker);
        quote.setOpen(open);
        quote.setHigh(high);
        quote.setLow(low);
        quote.setPrice(price);
        quote.setVolume(volume);
        quote.setLatestTradingDay(new java.sql.Date(System.currentTimeMillis()));
        quote.setPreviousClose(previousClose);
        quote.setChange(change);
        quote.setChangePercent(changePercent);
        return quote;
    }
}

