package ca.jrvs.apps.jdbc;



import ca.jrvs.apps.jdbc.STOCKQUOTE.Position;
import ca.jrvs.apps.jdbc.STOCKQUOTE.PositionDao;
import ca.jrvs.apps.jdbc.STOCKQUOTE.Quote;
import ca.jrvs.apps.jdbc.STOCKQUOTE.QuoteDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Optional;

import static org.junit.Assert.*;

public class PositionDao_IntTest {

    private Connection connection;
    private PositionDao positionDao;
    private QuoteDao quoteDao;

    @Before
    public void setUp() throws Exception {
        // Establish a connection to the test database
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/stock_quote_test", "postgres", "password");
        positionDao = new PositionDao(connection);
        quoteDao = new QuoteDao(connection);

        // Ensure the test environment is clean
        positionDao.deleteAll();
        quoteDao.deleteById("TSLA");

        // Add a new quote for TSLA to satisfy foreign key constraints
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

        quoteDao.save(tslaQuote); // Save the quote to ensure it exists in the quote table
    }

    @After
    public void tearDown() throws Exception {
        // Clean up the database after tests to maintain test isolation
        positionDao.deleteAll();
        quoteDao.deleteById("TSLA");
        connection.close();
    }

    @Test
    public void testSaveRetrieveAndDeletePosition() {
        // Save a new position for TSLA
        Position position = new Position();
        position.setTicker("TSLA");
        position.setNumOfShares(50);
        position.setValuePaid(36000.0);
        positionDao.save(position);

        // Retrieve the position and verify its properties
        Optional<Position> retrievedPosition = positionDao.findById("TSLA");
        assertTrue("Position should be present", retrievedPosition.isPresent());
        assertEquals("Number of shares should match", 50, retrievedPosition.get().getNumOfShares());
        assertEquals("Value paid should match", 36000.0, retrievedPosition.get().getValuePaid(), 0);

        // Delete the position
        positionDao.deleteById("TSLA");

        // Ensure the position is deleted
        Optional<Position> deletedPosition = positionDao.findById("TSLA");
        assertFalse("Position should be deleted", deletedPosition.isPresent());
    }

    @Test
    public void testUpdatePosition() {
        // Save an initial position for TSLA
        Position position = new Position();
        position.setTicker("TSLA");
        position.setNumOfShares(30);
        position.setValuePaid(21600.0);
        positionDao.save(position);

        // Update the position
        position.setNumOfShares(60);
        position.setValuePaid(43200.0);
        positionDao.save(position);

        // Retrieve and verify the updated position
        Optional<Position> updatedPosition = positionDao.findById("TSLA");
        assertTrue("Updated position should be present", updatedPosition.isPresent());
        assertEquals("Updated number of shares should match", 60, updatedPosition.get().getNumOfShares());
        assertEquals("Updated value paid should match", 43200.0, updatedPosition.get().getValuePaid(), 0);
    }

    @Test
    public void testFindAllNoPositions() {
        positionDao.deleteAll(); // Ensure there are no positions

        Iterable<Position> positions = positionDao.findAll();
        assertNotNull("The list of positions should not be null", positions);
        assertFalse("There should be no positions", positions.iterator().hasNext());
    }

    @Test
    public void testMultiplePositionOperations() {
        // Save multiple positions
        Position position1 = new Position();
        position1.setTicker("TSLA");
        position1.setNumOfShares(40);
        position1.setValuePaid(28800.0);
        positionDao.save(position1);

        Position position2 = new Position();
        position2.setTicker("TSLA");
        position2.setNumOfShares(20);
        position2.setValuePaid(14400.0);
        positionDao.save(position2);

        // Verify all positions can be retrieved
        Iterable<Position> positions = positionDao.findAll();
        assertNotNull("The list of positions should not be null", positions);
        int count = 0;
        for (Position pos : positions) {
            count++;
        }
        assertEquals("There should be 2 positions", 2, count);

        // Delete all positions
        positionDao.deleteAll();

        // Verify no positions remain
        positions = positionDao.findAll();
        assertFalse("All positions should be deleted", positions.iterator().hasNext());
    }
}
