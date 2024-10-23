package ca.jrvs.apps.jdbc.STOCKQUOTE;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class QuoteDao implements CrudDao<Quote, String> {

    private static final Logger logger = LogManager.getLogger(QuoteDao.class);
    private final Connection connection;

    public QuoteDao(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
    }

    @Override
    public Quote save(Quote quote) {
        String upsertSQL = "INSERT INTO quote (symbol, open, high, low, price, volume, latest_trading_day, previous_close, change, change_percent) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON CONFLICT (symbol) DO UPDATE SET open = EXCLUDED.open, high = EXCLUDED.high, low = EXCLUDED.low, "
                + "price = EXCLUDED.price, volume = EXCLUDED.volume, latest_trading_day = EXCLUDED.latest_trading_day, "
                + "previous_close = EXCLUDED.previous_close, change = EXCLUDED.change, change_percent = EXCLUDED.change_percent";

        try (PreparedStatement stmt = connection.prepareStatement(upsertSQL)) {
            setQuoteParameters(stmt, quote);
            stmt.executeUpdate();
            logger.info("Quote saved/updated successfully: " + quote);
            return quote;
        } catch (SQLException e) {
            String errorMsg = "Error saving/updating quote: " + quote;
            logger.error(errorMsg, e);
            throw new DataAccessException(errorMsg, e);
        }
    }

    @Override
    public Optional<Quote> findById(String symbol) {
        String selectSQL = "SELECT * FROM quote WHERE symbol = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectSQL)) {
            stmt.setString(1, symbol);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Quote quote = extractQuote(rs);
                    logger.info("Quote found for symbol: " + symbol);
                    return Optional.of(quote);
                }
            }
        } catch (SQLException e) {
            String errorMsg = "Error finding quote by symbol: " + symbol;
            logger.error(errorMsg, e);
            throw new DataAccessException(errorMsg, e);
        }
        logger.warn("No quote found for symbol: " + symbol);
        return Optional.empty();
    }

    @Override
    public Iterable<Quote> findAll() {
        String selectAllSQL = "SELECT * FROM quote";
        List<Quote> quotes = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(selectAllSQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                quotes.add(extractQuote(rs));
            }
            logger.info("Fetched " + quotes.size() + " quotes from the database.");
        } catch (SQLException e) {
            String errorMsg = "Error fetching all quotes";
            logger.error(errorMsg, e);
            throw new DataAccessException(errorMsg, e);
        }
        return quotes;
    }

    @Override
    public void deleteById(String symbol) {
        String deletePositionSQL = "DELETE FROM position WHERE symbol = ?";
        String deleteQuoteSQL = "DELETE FROM quote WHERE symbol = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement positionStmt = connection.prepareStatement(deletePositionSQL)) {
                positionStmt.setString(1, symbol);
                positionStmt.executeUpdate();
            }

            try (PreparedStatement quoteStmt = connection.prepareStatement(deleteQuoteSQL)) {
                quoteStmt.setString(1, symbol);
                quoteStmt.executeUpdate();
            }

            connection.commit();
            logger.info("Successfully deleted quote and associated positions for symbol: " + symbol);
        } catch (SQLException e) {
            logger.error("Error deleting quote and positions for symbol: " + symbol, e);
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                logger.error("Error rolling back transaction", rollbackEx);
            }
            throw new DataAccessException("Error deleting quote by symbol", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.error("Error resetting auto commit", e);
            }
        }
    }

    @Override
    public void deleteAll() {
        String deleteSQL = "DELETE FROM quote";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
            int rowsDeleted = stmt.executeUpdate();
            logger.info("Deleted " + rowsDeleted + " quotes from the database.");
        } catch (SQLException e) {
            String errorMsg = "Error deleting all quotes";
            logger.error(errorMsg, e);
            throw new DataAccessException(errorMsg, e);
        }
    }

    private void setQuoteParameters(PreparedStatement stmt, Quote quote) throws SQLException {
        stmt.setString(1, quote.getTicker());
        stmt.setDouble(2, quote.getOpen());
        stmt.setDouble(3, quote.getHigh());
        stmt.setDouble(4, quote.getLow());
        stmt.setDouble(5, quote.getPrice());
        stmt.setInt(6, quote.getVolume());
        stmt.setDate(7, quote.getLatestTradingDay());
        stmt.setDouble(8, quote.getPreviousClose());
        stmt.setDouble(9, quote.getChange());
        stmt.setString(10, quote.getChangePercent());
    }

    private Quote extractQuote(ResultSet rs) throws SQLException {
        Quote quote = new Quote();
        quote.setTicker(rs.getString("symbol"));
        quote.setOpen(rs.getDouble("open"));
        quote.setHigh(rs.getDouble("high"));
        quote.setLow(rs.getDouble("low"));
        quote.setPrice(rs.getDouble("price"));
        quote.setVolume(rs.getInt("volume"));
        quote.setLatestTradingDay(rs.getDate("latest_trading_day"));
        quote.setPreviousClose(rs.getDouble("previous_close"));
        quote.setChange(rs.getDouble("change"));
        quote.setChangePercent(rs.getString("change_percent"));
        return quote;
    }
}

