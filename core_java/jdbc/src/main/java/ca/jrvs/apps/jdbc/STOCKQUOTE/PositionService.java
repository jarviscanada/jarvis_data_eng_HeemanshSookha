package ca.jrvs.apps.jdbc.STOCKQUOTE;


import java.util.Optional;

public class PositionService {

    private final PositionDao positionDao;
    private final QuoteService quoteService;

    public PositionService(PositionDao positionDao, QuoteService quoteService) {
        if (positionDao == null || quoteService == null) {
            throw new IllegalArgumentException("DAO and QuoteService cannot be null");
        }
        this.positionDao = positionDao;
        this.quoteService = quoteService;
    }

    /**
     * Processes a buy order and updates the database accordingly.
     *
     * @param ticker          the stock ticker symbol
     * @param numberOfShares  the number of shares to buy
     * @param price           the price per share
     * @return the updated Position in the database after processing the buy
     * @throws IllegalArgumentException if the input is invalid or the ticker symbol is not found
     */
    public Position buy(String ticker, int numberOfShares, double price) {
        validateBuyOrder(ticker, numberOfShares, price);

        // Fetch the latest quote
        Quote latestQuote = quoteService.fetchQuoteDataFromAPI(ticker)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ticker symbol: " + ticker));

        // Ensure there is enough volume to buy
        if (numberOfShares > latestQuote.getVolume()) {
            throw new IllegalArgumentException("Cannot buy more than available volume: " + latestQuote.getVolume());
        }

        // Fetch current position or create a new one
        Position position = positionDao.findById(ticker).orElseGet(() -> new Position(ticker));
        updatePosition(position, numberOfShares, price);

        // Save the updated position
        return positionDao.save(position);
    }

    /**
     * Retrieves the position for a given stock ticker symbol.
     *
     * @param ticker the stock ticker symbol
     * @return an Optional containing the Position if found, otherwise empty
     */
    public Optional<Position> getPositionForStock(String ticker) {
        return positionDao.findById(ticker);
    }

    /**
     * Sells all shares of the given ticker symbol by deleting the position.
     *
     * @param ticker the stock ticker symbol
     */
    public void sell(String ticker) {
        if (ticker == null || ticker.isEmpty()) {
            throw new IllegalArgumentException("Ticker symbol cannot be null or empty");
        }
        positionDao.deleteById(ticker);
    }

    /**
     * Validates the buy order parameters.
     *
     * @param ticker          the stock ticker symbol
     * @param numberOfShares  the number of shares to buy
     * @param price           the price per share
     */
    private void validateBuyOrder(String ticker, int numberOfShares, double price) {
        if (ticker == null || ticker.trim().isEmpty()) {
            throw new IllegalArgumentException("Ticker symbol cannot be null or empty");
        }
        if (numberOfShares <= 0) {
            throw new IllegalArgumentException("Number of shares must be positive");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }

    /**
     * Updates the position with the new shares and value paid.
     *
     * @param position        the current position
     * @param numberOfShares  the number of shares to add
     * @param price           the price per share
     */
    private void updatePosition(Position position, int numberOfShares, double price) {
        position.setNumOfShares(position.getNumOfShares() + numberOfShares);
        position.setValuePaid(position.getValuePaid() + (numberOfShares * price));
    }
}

