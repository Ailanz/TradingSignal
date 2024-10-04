package org.tradingsignal.stock;

import lombok.Data;
import org.tradingsignal.pojo.yahoo.Meta;
import org.tradingsignal.pojo.yahoo.Quote;
import org.tradingsignal.pojo.yahoo.StockPrice;

import java.util.ArrayList;
import java.util.List;

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
        stockPrice.getChart().getResult().get(0).getEvents().getDividends().forEach((key, value) -> {
            DateDividends dateDividends = new DateDividends();
            dateDividends.setTimestamp(Long.parseLong(key));
            dateDividends.setValue(value.getAmount());
            dividends.add(dateDividends);
        });
        stockData.setDividends(dividends);
        return stockData;
    }
}
