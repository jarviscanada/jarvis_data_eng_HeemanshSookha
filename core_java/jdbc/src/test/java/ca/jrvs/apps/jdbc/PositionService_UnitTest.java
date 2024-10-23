package ca.jrvs.apps.jdbc;

import ca.jrvs.apps.jdbc.STOCKQUOTE.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PositionService_UnitTest {

    private PositionService positionService;
    private PositionDao positionDao;
    private QuoteService quoteService;

    @Before
    public void setup() {
        positionDao = mock(PositionDao.class);
        quoteService = mock(QuoteService.class);
        positionService = new PositionService(positionDao, quoteService);
    }

    @Test
    public void testBuyValidScenario() {
        // Mocking QuoteService to provide a valid quote
        Quote quote = new Quote();
        quote.setTicker("TSLA");
        quote.setPrice(700.0);
        quote.setVolume(1000);
        when(quoteService.fetchQuoteDataFromAPI("TSLA")).thenReturn(Optional.of(quote));

        // Mocking PositionDao to return the saved position
        Position position = new Position();
        position.setTicker("TSLA");
        position.setNumOfShares(10);
        position.setValuePaid(7000.0);
        when(positionDao.save(any(Position.class))).thenReturn(position);

        // Execute the buy operation
        Position result = positionService.buy("TSLA", 10, 700.0);

        // Verify the results
        assertNotNull(result);
        assertEquals("TSLA", result.getTicker());
        assertEquals(10, result.getNumOfShares());
        assertEquals(7000.0, result.getValuePaid(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyWithInvalidTicker() {
        when(quoteService.fetchQuoteDataFromAPI("INVALID")).thenReturn(Optional.empty());
        positionService.buy("INVALID", 10, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyWithInvalidSharesAndPrice() {
        positionService.buy("TSLA", -10, -100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuyExceedsAvailableVolume() {
        Quote quote = new Quote();
        quote.setTicker("TSLA");
        quote.setVolume(5);  // Low volume
        when(quoteService.fetchQuoteDataFromAPI("TSLA")).thenReturn(Optional.of(quote));

        positionService.buy("TSLA", 10, 700.0);  // Exceeds volume
    }

    @Test
    public void testGetPositionForStock() {
        Position position = new Position();
        position.setTicker("TSLA");
        position.setNumOfShares(5);
        position.setValuePaid(3500.0);
        when(positionDao.findById("TSLA")).thenReturn(Optional.of(position));

        Optional<Position> result = positionService.getPositionForStock("TSLA");
        assertTrue(result.isPresent());
        assertEquals("TSLA", result.get().getTicker());
        assertEquals(5, result.get().getNumOfShares());
        assertEquals(3500.0, result.get().getValuePaid(), 0);
    }

    @Test
    public void testSellValidScenario() {
        positionService.sell("TSLA");
        verify(positionDao, times(1)).deleteById("TSLA");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSellWithInvalidTicker() {
        positionService.sell("");
    }
}

