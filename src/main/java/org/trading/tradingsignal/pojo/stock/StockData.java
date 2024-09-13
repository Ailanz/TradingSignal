package org.trading.tradingsignal.pojo.stock;

import lombok.Data;
import org.trading.tradingsignal.pojo.yahoo.Meta;
import org.trading.tradingsignal.pojo.yahoo.Quote;
import org.trading.tradingsignal.pojo.yahoo.Result;
import org.trading.tradingsignal.pojo.yahoo.StockPrice;

import java.util.ArrayList;
import java.util.List;

@Data
public class StockData {
    private String symbol;
    private String exchangeName;
    private double regularMarketPrice;
    private List<DatePrice> datePrices;


    public static StockData fromStockPrice(StockPrice stockPrice) {
        StockData stockData = new StockData();
        Meta meta = stockPrice.getChart().getResult().get(0).getMeta();
        stockData.setSymbol(meta.getSymbol());
        stockData.setExchangeName(meta.getExchangeName());
        stockData.setRegularMarketPrice(meta.getRegularMarketPrice());

        List<DatePrice> datePrices = new ArrayList<>();
        List<Integer> timestamps = stockPrice.getChart().getResult().get(0).getTimestamp();
        Quote quote = stockPrice.getChart().getResult().get(0).getIndicators().getQuote().get(0);

        for (int i = 0; i < timestamps.size(); i++) {
            DatePrice datePrice = new DatePrice();
            datePrice.setTimestamp(timestamps.get(i));
            datePrice.setOpen(quote.getOpen().get(i));
            datePrice.setHigh(quote.getHigh().get(i));
            datePrice.setLow(quote.getLow().get(i));
            datePrice.setClose(quote.getClose().get(i));
            datePrices.add(datePrice);
        }

        stockData.setDatePrices(datePrices);
        return stockData;
    }

// getters and setters
}
