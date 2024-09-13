package org.trading.tradingsignal.pojo;

import lombok.Data;
import org.trading.tradingsignal.pojo.yahoo.Meta;
import org.trading.tradingsignal.pojo.yahoo.Quote;
import org.trading.tradingsignal.pojo.yahoo.Result;
import org.trading.tradingsignal.pojo.yahoo.StockPrice;

import java.util.List;

@Data
public class StockData {
    private String symbol;
    private String exchangeName;
    private double regularMarketPrice;
    private List<Integer> timestamps;
    private List<Double> opens;
    private List<Double> highs;
    private List<Double> lows;
    private List<Double> closes;

    public StockData(StockPrice stockPrice) {
        Result result = stockPrice.getChart().getResult().get(0);
        Meta meta = result.getMeta();
        Quote quote = result.getIndicators().getQuote().get(0);

        this.symbol = meta.getSymbol();
        this.exchangeName = meta.getExchangeName();
        this.regularMarketPrice = meta.getRegularMarketPrice();
        this.timestamps = result.getTimestamp();
        this.opens = quote.getOpen();
        this.highs = quote.getHigh();
        this.lows = quote.getLow();
        this.closes = quote.getClose();
    }

    // getters and setters
}
