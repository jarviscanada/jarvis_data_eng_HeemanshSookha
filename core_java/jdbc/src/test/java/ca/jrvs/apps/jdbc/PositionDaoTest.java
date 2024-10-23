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

public class PositionDaoTest {

    private Connection connection;
    private PositionDao positionDao;
    private QuoteDao quoteDao;

    @Before
    public void setUp() throws Exception {
        // Establish a connection to the test database
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/stock_quote_test", "postgres", "password");
        positionDao = new PositionDao(connection);
        quoteDao = new QuoteDao(connection);

        // Create a test quote in the database to satisfy foreign key constraints
        Quote testQuote = new Quote();
        testQuote.setTicker("GOOGL");
        testQuote.setOpen(2800.0);
        testQuote.setHigh(2850.0);
        testQuote.setLow(2750.0);
        testQuote.setPrice(2820.0);
        testQuote.setVolume(10000);
        testQuote.setLatestTradingDay(new java.sql.Date(System.currentTimeMillis()));
        testQuote.setPreviousClose(2790.0);
        testQuote.setChange(30.0);
        testQuote.setChangePercent("1.08%");

        quoteDao.save(testQuote);
    }

    @After
    public void tearDown() throws Exception {
        // Clean up the database by deleting all positions and quotes after each test
        positionDao.deleteAll();
        quoteDao.deleteById("GOOGL");
        connection.close();
    }

    @Test
    public void testSaveAndRetrievePosition() {
        Position position = new Position();
        position.setTicker("GOOGL");
        position.setNumOfShares(50);
        position.setValuePaid(141000.0);

        // Save the position
        positionDao.save(position);

        // Retrieve the position and verify its properties
        Optional<Position> retrievedPosition = positionDao.findById("GOOGL");
        assertTrue("The position should be present", retrievedPosition.isPresent());
        assertEquals("The number of shares should match", 50, retrievedPosition.get().getNumOfShares());
        assertEquals("The value paid should match", 141000.0, retrievedPosition.get().getValuePaid(), 0);
    }

    @Test
    public void testUpdatePosition() {
        Position position = new Position();
        position.setTicker("GOOGL");
        position.setNumOfShares(50);
        position.setValuePaid(141000.0);

        // Save the initial position
        positionDao.save(position);

        // Update the position
        position.setNumOfShares(75);
        position.setValuePaid(210000.0);
        positionDao.save(position);

        // Retrieve the updated position and verify changes
        Optional<Position> updatedPosition = positionDao.findById("GOOGL");
        assertTrue("The updated position should be present", updatedPosition.isPresent());
        assertEquals("The number of shares should match the updated value", 75, updatedPosition.get().getNumOfShares());
        assertEquals("The value paid should match the updated value", 210000.0, updatedPosition.get().getValuePaid(), 0);
    }

    @Test
    public void testDeletePosition() {
        Position position = new Position();
        position.setTicker("GOOGL");
        position.setNumOfShares(50);
        position.setValuePaid(141000.0);

        // Save the position
        positionDao.save(position);

        // Delete the position
        positionDao.deleteById("GOOGL");

        // Verify the position is deleted
        Optional<Position> deletedPosition = positionDao.findById("GOOGL");
        assertFalse("The position should be deleted", deletedPosition.isPresent());
    }

    @Test
    public void testFindAllPositions() {
        Position position1 = new Position();
        position1.setTicker("GOOGL");
        position1.setNumOfShares(50);
        position1.setValuePaid(141000.0);

        Position position2 = new Position();
        position2.setTicker("AAPL");
        position2.setNumOfShares(30);
        position2.setValuePaid(45000.0);

        // Save both positions
        positionDao.save(position1);
        positionDao.save(position2);

        // Verify all positions can be retrieved
        Iterable<Position> positions = positionDao.findAll();
        assertNotNull("The list of positions should not be null", positions);
        int count = 0;
        for (Position pos : positions) {
            count++;
        }
        assertEquals("There should be 2 positions", 2, count);
    }

    @Test
    public void testDeleteAllPositions() {
        Position position = new Position();
        position.setTicker("GOOGL");
        position.setNumOfShares(50);
        position.setValuePaid(141000.0);

        // Save the position
        positionDao.save(position);

        // Delete all positions
        positionDao.deleteAll();

        // Verify no positions remain
        Iterable<Position> positions = positionDao.findAll();
        assertFalse("All positions should be deleted", positions.iterator().hasNext());
    }
}

