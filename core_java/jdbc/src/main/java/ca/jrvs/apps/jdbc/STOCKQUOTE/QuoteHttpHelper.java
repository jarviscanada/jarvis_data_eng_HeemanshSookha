package ca.jrvs.apps.jdbc.STOCKQUOTE;



import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

public class QuoteHttpHelper {

    private static final Logger logger = LogManager.getLogger(QuoteHttpHelper.class);

    private static final String API_KEY = "API-KEY";
    private static final String BASE_URL = "https://alpha-vantage.p.rapidapi.com/query";
    private static final String HOST_HEADER = "alpha-vantage.p.rapidapi.com";
    private static final String API_KEY_HEADER = "x-rapidapi-key";

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public QuoteHttpHelper(OkHttpClient mockClient) {
        this.client = new OkHttpClient();
        this.objectMapper = new ObjectMapper();
    }

    public Quote fetchQuoteInfo(String symbol) {
        HttpUrl url = buildRequestUrl(symbol);
        Request request = buildRequest(url);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = "Failed to fetch data from API for symbol: " + symbol + " with response: " + response;
                logger.error(errorMsg);
                throw new IOException(errorMsg);
            }

            return parseResponse(response, symbol);
        } catch (IOException e) {
            logger.error("Error fetching quote information for symbol: " + symbol, e);
            throw new RuntimeException("Failed to fetch data from API", e);
        }
    }

    private HttpUrl buildRequestUrl(String symbol) {
        return HttpUrl.parse(BASE_URL).newBuilder()
                .addQueryParameter("function", "GLOBAL_QUOTE")
                .addQueryParameter("symbol", symbol)
                .addQueryParameter("datatype", "json")
                .build();
    }

    private Request buildRequest(HttpUrl url) {
        return new Request.Builder()
                .url(url)
                .addHeader("x-rapidapi-host", HOST_HEADER)
                .addHeader(API_KEY_HEADER, API_KEY)
                .build();
    }

    private Quote parseResponse(Response response, String symbol) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(response.body().string()).get("Global Quote");
        if (jsonNode == null || !jsonNode.has("01. symbol")) {
            String errorMsg = "Invalid or missing symbol data for: " + symbol;
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        return mapJsonToQuote(jsonNode);
    }

    private Quote mapJsonToQuote(JsonNode jsonNode) {
        Quote quote = new Quote();
        quote.setTicker(jsonNode.get("01. symbol").asText());
        quote.setOpen(jsonNode.get("02. open").asDouble());
        quote.setHigh(jsonNode.get("03. high").asDouble());
        quote.setLow(jsonNode.get("04. low").asDouble());
        quote.setPrice(jsonNode.get("05. price").asDouble());
        quote.setVolume(jsonNode.get("06. volume").asInt());
        quote.setLatestTradingDay(java.sql.Date.valueOf(jsonNode.get("07. latest trading day").asText()));
        quote.setPreviousClose(jsonNode.get("08. previous close").asDouble());
        quote.setChange(jsonNode.get("09. change").asDouble());
        quote.setChangePercent(jsonNode.get("10. change percent").asText());

        return quote;
    }
}

