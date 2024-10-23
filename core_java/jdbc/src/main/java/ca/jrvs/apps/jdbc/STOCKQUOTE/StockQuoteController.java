package ca.jrvs.apps.jdbc.STOCKQUOTE;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Optional;
import java.util.Scanner;

public class StockQuoteController {

    private static final Logger logger = LogManager.getLogger(StockQuoteController.class);

    private final QuoteService quoteService;
    private final PositionService positionService;

    public StockQuoteController(QuoteService quoteService, PositionService positionService) {
        if (quoteService == null || positionService == null) {
            throw new IllegalArgumentException("Services cannot be null");
        }
        this.quoteService = quoteService;
        this.positionService = positionService;
    }

    /**
     * User interface for our application.
     */
    public void initClient() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            logger.info("Initializing Stock Quote App client");

            System.out.println("**********************");
            System.out.println("Welcome to the Stock Quote App! Choose an option:");
            System.out.println("1. View Stock Information");
            System.out.println("2. Buy Stock");
            System.out.println("3. Sell Stock");
            System.out.println("4. Exit");
            System.out.println("**********************");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        viewStockInformation(scanner);
                        break;
                    case "2":
                        buyStock(scanner);
                        break;
                    case "3":
                        sellStock(scanner);
                        break;
                    case "4":
                        logger.info("Exiting Stock Quote App");
                        System.out.println("Thank you for using the Stock Quote App. Goodbye!");
                        return;
                    default:
                        logger.warn("Invalid option selected: " + choice);
                        System.out.println("Invalid option. Please choose 1, 2, 3, or 4.");
                        break;
                }
            } catch (Exception e) {
                logger.error("Error occurred in StockQuoteController: " + e.getMessage(), e);
                System.out.println("An unexpected error occurred. Please try again.");
            }
        }
    }

    /**
     * View stock information before making a decision.
     */
    private void viewStockInformation(Scanner scanner) {
        logger.info("Fetching stock information");
        System.out.println("Available stocks:");

        Iterable<Quote> allQuotes = quoteService.getAllQuotes();
        if (!allQuotes.iterator().hasNext()) {
            System.out.println("No stocks available in the database.");
            return;
        }

        for (Quote quote : allQuotes) {
            System.out.println("Ticker: " + quote.getTicker() + " | Current Price: " + quote.getPrice());
        }

        System.out.println("Enter the ticker symbol of the stock to view detailed information:");
        String ticker = scanner.nextLine().trim();

        try {
            Optional<Quote> quote = quoteService.fetchQuoteDataFromAPI(ticker);
            if (quote.isPresent()) {
                displayQuoteInfo(quote.get(), ticker);
            } else {
                logger.warn("No stock information found for ticker: " + ticker);
                System.out.println("No data found for ticker symbol: " + ticker);
            }
        } catch (Exception e) {
            logger.error("Error occurred while viewing stock information: " + e.getMessage(), e);
            System.out.println("An error occurred while fetching stock information. Please try again.");
        }
    }

    private void displayQuoteInfo(Quote quote, String ticker) {
        logger.info("Displaying stock information for " + ticker);
        System.out.println("Stock Information for " + ticker + ":");
        System.out.println("Current Price: " + quote.getPrice());
        System.out.println("Open: " + quote.getOpen());
        System.out.println("High: " + quote.getHigh());
        System.out.println("Low: " + quote.getLow());
        System.out.println("Volume: " + quote.getVolume());

        Optional<Position> position = positionService.getPositionForStock(ticker);
        if (position.isPresent()) {
            double pricePaidPerShare = position.get().getValuePaid() / position.get().getNumOfShares();
            System.out.println("You own " + position.get().getNumOfShares() + " shares.");
            System.out.println("Price paid per share: $" + pricePaidPerShare);
            System.out.println("Total amount paid: $" + position.get().getValuePaid());
        } else {
            logger.warn("No stock for ticker: " + ticker + " found in the portfolio");
            System.out.println("You do not own any shares of this stock.");
        }
    }

    /**
     * Buy stock for a given ticker.
     */
    private void buyStock(Scanner scanner) {
        logger.info("Starting buy stock process");

        System.out.println("Available stocks to buy:");
        Iterable<Quote> allQuotes = quoteService.getAllQuotes();
        if (!allQuotes.iterator().hasNext()) {
            System.out.println("No stocks available to buy.");
            return;
        }

        for (Quote quote : allQuotes) {
            System.out.println("Ticker: " + quote.getTicker() + " | Current Price: " + quote.getPrice());
        }

        System.out.println("Enter the ticker symbol of the stock you want to buy:");
        String ticker = scanner.nextLine().trim();

        int shares = promptForShares(scanner);
        double price = promptForPrice(scanner);

        try {
            Position position = positionService.buy(ticker, shares, price);
            System.out.println("You have successfully bought " + shares + " shares of " + ticker + " at $" + price + " per share.");
            System.out.println("Your new position: " + position.getNumOfShares() + " shares, total paid: $" + position.getValuePaid());
        } catch (IllegalArgumentException e) {
            logger.error("Error during stock purchase: " + e.getMessage(), e);
            System.out.println("Error: " + e.getMessage());
            retryPurchase(scanner);
        }
    }

    private void retryPurchase(Scanner scanner) {
        System.out.println("Would you like to try again? Type 'yes' to try again, or 'no' to cancel:");
        String retryChoice = scanner.nextLine().trim();
        if (!retryChoice.equalsIgnoreCase("yes")) {
            System.out.println("Stock purchase canceled.");
        }
    }

    private int promptForShares(Scanner scanner) {
        while (true) {
            System.out.println("Enter the number of shares you want to buy:");
            try {
                int shares = Integer.parseInt(scanner.nextLine().trim());
                if (shares > 0) {
                    return shares;
                } else {
                    System.out.println("Number of shares must be positive. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid integer.");
            }
        }
    }

    private double promptForPrice(Scanner scanner) {
        while (true) {
            System.out.println("Enter the price per share you are willing to pay:");
            try {
                double price = Double.parseDouble(scanner.nextLine().trim());
                if (price > 0) {
                    return price;
                } else {
                    System.out.println("Price must be positive. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid price. Please enter a valid number.");
            }
        }
    }

    /**
     * Sell all shares of a given ticker.
     */
    private void sellStock(Scanner scanner) {
        logger.info("Starting sell stock process");
        System.out.println("Enter the ticker symbol of the stock you want to sell:");
        String ticker = scanner.nextLine().trim();

        Optional<Position> position = positionService.getPositionForStock(ticker);
        if (!position.isPresent()) {
            System.out.println("You do not own any shares of " + ticker);
            return;
        }

        System.out.println("You own " + position.get().getNumOfShares() + " shares of " + ticker + ".");
        System.out.println("Are you sure you want to sell all shares? Type 'yes' to confirm, 'no' to cancel:");
        String confirmation = scanner.nextLine().trim();

        if (confirmation.equalsIgnoreCase("yes")) {
            try {
                positionService.sell(ticker);
                System.out.println("You have successfully sold all shares of " + ticker);
            } catch (IllegalArgumentException e) {
                logger.error("Error during stock sale: " + e.getMessage(), e);
                System.out.println("Error: " + e.getMessage());
            }
        } else {
            logger.info("Sale of " + ticker + " canceled.");
            System.out.println("Sale of " + ticker + " canceled.");
        }
    }
}
