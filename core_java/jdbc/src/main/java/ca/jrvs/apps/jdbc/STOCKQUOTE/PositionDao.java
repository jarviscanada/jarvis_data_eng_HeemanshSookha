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

public class PositionDao implements CrudDao<Position, String> {

    private static final Logger logger = LogManager.getLogger(PositionDao.class);
    private final Connection connection;

    public PositionDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Position save(Position position) {
        String upsertSQL = "INSERT INTO position (symbol, number_of_shares, value_paid) "
                + "VALUES (?, ?, ?) "
                + "ON CONFLICT (symbol) DO UPDATE SET number_of_shares = EXCLUDED.number_of_shares, value_paid = EXCLUDED.value_paid";

        try (PreparedStatement stmt = connection.prepareStatement(upsertSQL)) {
            stmt.setString(1, position.getTicker());
            stmt.setInt(2, position.getNumOfShares());
            stmt.setDouble(3, position.getValuePaid());
            stmt.executeUpdate();
            logger.info("Position saved/updated successfully: " + position);
            return position;
        } catch (SQLException e) {
            logger.error("Error saving position: " + position, e);
            throw new IllegalArgumentException("Error saving position", e);
        }
    }

    @Override
    public Optional<Position> findById(String symbol) {
        String selectSQL = "SELECT * FROM position WHERE symbol = ?";
        try (PreparedStatement stmt = connection.prepareStatement(selectSQL)) {
            stmt.setString(1, symbol);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Position position = extractPosition(rs);
                    logger.info("Position found: " + position);
                    return Optional.of(position);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding position by id: " + symbol, e);
            throw new IllegalArgumentException("Error finding position by id", e);
        }
        logger.info("Position not found for symbol: " + symbol);
        return Optional.empty();
    }

    @Override
    public Iterable<Position> findAll() {
        String selectAllSQL = "SELECT * FROM position";
        List<Position> positions = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(selectAllSQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                positions.add(extractPosition(rs));
            }
            logger.info("Fetched " + positions.size() + " positions from the database.");
        } catch (SQLException e) {
            logger.error("Error fetching all positions", e);
            throw new IllegalArgumentException("Error fetching all positions", e);
        }
        return positions;
    }

    @Override
    public void deleteById(String symbol) {
        String deleteSQL = "DELETE FROM position WHERE symbol = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
            stmt.setString(1, symbol);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Position deleted successfully for symbol: " + symbol);
            } else {
                logger.info("No position found to delete for symbol: " + symbol);
            }
        } catch (SQLException e) {
            logger.error("Error deleting position by id: " + symbol, e);
            throw new IllegalArgumentException("Error deleting position by id", e);
        }
    }

    @Override
    public void deleteAll() {
        String deleteSQL = "DELETE FROM position";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSQL)) {
            int rowsAffected = stmt.executeUpdate();
            logger.info("Deleted " + rowsAffected + " positions from the database.");
        } catch (SQLException e) {
            logger.error("Error deleting all positions", e);
            throw new IllegalArgumentException("Error deleting all positions", e);
        }
    }

    private Position extractPosition(ResultSet rs) throws SQLException {
        Position position = new Position();
        position.setTicker(rs.getString("symbol"));
        position.setNumOfShares(rs.getInt("number_of_shares"));
        position.setValuePaid(rs.getDouble("value_paid"));
        return position;
    }
}

