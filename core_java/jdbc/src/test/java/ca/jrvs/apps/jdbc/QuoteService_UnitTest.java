package ca.jrvs.apps.jdbc;



import ca.jrvs.apps.jdbc.STOCKQUOTE.Quote;
import ca.jrvs.apps.jdbc.STOCKQUOTE.QuoteDao;
import ca.jrvs.apps.jdbc.STOCKQUOTE.QuoteHttpHelper;
import ca.jrvs.apps.jdbc.STOCKQUOTE.QuoteService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteService_UnitTest {

    private QuoteService quoteService;
    private QuoteDao quoteDao;
    private QuoteHttpHelper httpHelper;

    @Before
    public void setup() {
        quoteDao = mock(QuoteDao.class);
        httpHelper = mock(QuoteHttpHelper.class);
        quoteService = new QuoteService(quoteDao, httpHelper);
    }

    @Test
    public void testFetchQuoteDataFromAPI_validTicker() throws IOException {
        // Arrange: Set up the mock behavior for a valid quote
        Quote quote = new Quote();
        quote.setTicker("AAPL");
        quote.setPrice(150.0);
        when(httpHelper.fetchQuoteInfo("AAPL")).thenReturn(quote);

        // Act: Call the method under test
        Optional<Quote> result = quoteService.fetchQuoteDataFromAPI("AAPL");

        // Assert: Verify the expected outcomes
        assertTrue("Result should be present", result.isPresent());
        assertEquals("AAPL", result.get().getTicker());
        assertEquals(150.0, result.get().getPrice(), 0);

        // Verify that the quote was saved to the database
        verify(quoteDao, times(1)).save(quote);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFetchQuoteDataFromAPI_invalidTicker() {
        // Act & Assert: Call the method with an invalid ticker and expect an exception
        quoteService.fetchQuoteDataFromAPI("");
    }

    @Test
    public void testFetchQuoteDataFromAPI_notFound() throws IOException {
        // Arrange: Simulate the case where the quote is not found
        when(httpHelper.fetchQuoteInfo("XYZ")).thenThrow(new IOException("Quote not found"));

        // Act: Call the method with a ticker that doesn't exist
        Optional<Quote> result = quoteService.fetchQuoteDataFromAPI("XYZ");

        // Assert: Verify that the result is absent
        assertFalse("Result should not be present", result.isPresent());
    }

    @Test(expected = RuntimeException.class)
    public void testFetchQuoteDataFromAPI_networkError() throws IOException {
        // Arrange: Simulate a network error
        when(httpHelper.fetchQuoteInfo("AAPL")).thenThrow(new IOException("Network error"));

        // Act & Assert: Call the method and expect a RuntimeException
        quoteService.fetchQuoteDataFromAPI("AAPL");
    }
}

