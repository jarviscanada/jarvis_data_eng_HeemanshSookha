package ca.jrvs.apps.jdbc;



import ca.jrvs.apps.jdbc.STOCKQUOTE.Quote;
import ca.jrvs.apps.jdbc.STOCKQUOTE.QuoteHttpHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class QuoteHttpHelperTestMock {

    private QuoteHttpHelper quoteHttpHelper;
    private OkHttpClient mockClient;
    private Response mockResponse;
    private ResponseBody mockResponseBody;

    @Before
    public void setUp() throws IOException {
        mockClient = mock(OkHttpClient.class);
        mockResponse = mock(Response.class);
        mockResponseBody = mock(ResponseBody.class);

        // Initialize QuoteHttpHelper with mock OkHttpClient
        quoteHttpHelper = new QuoteHttpHelper(mockClient);

        // Mock a successful response
        String fakeApiResponse = "{ \"Global Quote\": { " +
                "\"01. symbol\": \"AAPL\", " +
                "\"02. open\": \"150.00\", " +
                "\"03. high\": \"155.00\", " +
                "\"04. low\": \"148.00\", " +
                "\"05. price\": \"152.00\", " +
                "\"06. volume\": \"10000\", " +
                "\"07. latest trading day\": \"2024-10-09\", " +
                "\"08. previous close\": \"150.00\", " +
                "\"09. change\": \"2.00\", " +
                "\"10. change percent\": \"1.33%\" } }";

        when(mockResponseBody.string()).thenReturn(fakeApiResponse);
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);
        when(mockClient.newCall(any(Request.class)).execute()).thenReturn(mockResponse);
    }

    @Test
    public void fetchQuoteInfo_validSymbol() throws IOException {
        Quote quote = quoteHttpHelper.fetchQuoteInfo("AAPL");

        // Validate that the mocked response data is processed correctly
        assertNotNull("Quote should not be null", quote);
        assertEquals("AAPL", quote.getTicker());
        assertEquals(150.00, quote.getOpen(), 0);
        assertEquals(155.00, quote.getHigh(), 0);
        assertEquals(148.00, quote.getLow(), 0);
        assertEquals(152.00, quote.getPrice(), 0);
        assertEquals(10000, quote.getVolume());
        assertEquals("2024-10-09", quote.getLatestTradingDay().toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void fetchQuoteInfo_invalidSymbol() throws IOException {
        // Mock an invalid response
        String invalidApiResponse = "{}";
        when(mockResponseBody.string()).thenReturn(invalidApiResponse);

        // Mock the response setup for an invalid symbol
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(mockResponseBody);

        // Try fetching an invalid symbol and expect an exception
        quoteHttpHelper.fetchQuoteInfo("INVALID");
    }

    @Test(expected = IOException.class)
    public void fetchQuoteInfo_networkError() throws IOException {
        // Mock a network error
        when(mockClient.newCall(any(Request.class)).execute()).thenThrow(new IOException("Network error"));

        // Try fetching a quote and expect an IOException
        quoteHttpHelper.fetchQuoteInfo("AAPL");
    }

    @Test(expected = IllegalArgumentException.class)
    public void fetchQuoteInfo_unsuccessfulResponse() throws IOException {
        // Mock an unsuccessful response
        when(mockResponse.isSuccessful()).thenReturn(false);

        // Try fetching a quote and expect an IllegalArgumentException due to unsuccessful response
        quoteHttpHelper.fetchQuoteInfo("AAPL");
    }
}
