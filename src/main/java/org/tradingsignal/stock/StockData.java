package org.tradingsignal.stock;

import lombok.Data;
import org.tradingsignal.pojo.yahoo.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class StockData {
    private String symbol;
    private String exchangeName;
    private double regularMarketPrice;
    private List<DatePrice> datePrices;
    private List<DateDividends> dividends;


    public static StockData fromStockPrice(StockPrice stockPrice) {
        StockData stockData = new StockData();
        Meta meta = stockPrice.getChart().getResult().get(0).getMeta();
        stockData.setSymbol(meta.getSymbol());
        stockData.setExchangeName(meta.getExchangeName());
        stockData.setRegularMarketPrice(meta.getRegularMarketPrice());

        List<DatePrice> datePrices = new ArrayList<>();
        List<Long> timestamps = stockPrice.getChart().getResult().get(0).getTimestamp();
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

        //set dividends
        List<DateDividends> dividends = new ArrayList<>();
        try {
            Events events = stockPrice.getChart().getResult().get(0).getEvents();
            if (events == null) {
                return stockData;
            }
            Map<String, Dividend> dividendMap = stockPrice.getChart().getResult().get(0).getEvents().getDividends();
            if (dividendMap != null) {
                dividendMap.forEach((key, value) -> {
                    DateDividends dateDividends = new DateDividends();
                    dateDividends.setTimestamp(Long.parseLong(key));
                    dateDividends.setValue(value.getAmount());
                    dividends.add(dateDividends);
                });
                stockData.setDividends(dividends);
            }
        } catch (Exception e) {
            System.out.println("No dividends for " + stockData.getSymbol());
        }

        return stockData;
    }
}
