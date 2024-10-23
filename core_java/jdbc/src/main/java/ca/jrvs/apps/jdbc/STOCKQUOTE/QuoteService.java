package ca.jrvs.apps.jdbc.STOCKQUOTE;



import ca.jrvs.apps.jdbc.STOCKQUOTE.QuoteDao;
import ca.jrvs.apps.jdbc.STOCKQUOTE.QuoteHttpHelper;

import java.io.IOException;
import java.util.Optional;

public class QuoteService {

    private QuoteDao dao;
    private QuoteHttpHelper httpHelper;

    public QuoteService(QuoteDao dao, QuoteHttpHelper httpHelper) {
        this.dao = dao;
        this.httpHelper = httpHelper;
    }


    public Optional<Quote> fetchQuoteDataFromAPI(String ticker) {

        if (ticker == null || ticker.isEmpty()) {
            throw new IllegalArgumentException("Invalid ticker symbol");
        }

        try {

            Quote quote = httpHelper.fetchQuoteInfo(ticker);

            dao.save(quote);
            return Optional.of(quote);
        } catch (IOException e) {

            return Optional.empty();
        }
    }


    public Iterable<Quote> getAllQuotes() {
        return dao.findAll();
    }
}