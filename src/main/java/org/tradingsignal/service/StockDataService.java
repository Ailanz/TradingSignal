package org.tradingsignal.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.tradingsignal.pojo.yahoo.StockPrice;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockConfigBuilder;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.util.DateCalc;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Log4j2
@Service
public class StockDataService {

    private static final HashMap<String, StockData> stockDataCache = new HashMap<>();

    private StockData cashData = null;

    @PostConstruct
    public void init() {
        StockData stockData = getStockPrice(
                StockConfigBuilder.builder()
                        .symbol("SPY")
                        .fromPeriod(0L)
                        .toPeriod(DateCalc.now())
                        .interval(StockConfigBuilder.Interval.ONE_DAY)
                        .build()
        );
        StockData cashData = new StockData();
        cashData.setSymbol(Asset.CASH);
        cashData.setDatePrices(new LinkedList<>());
        for (DatePrice datePrice : stockData.getDatePrices()) {
            cashData.getDatePrices().add(new DatePrice(datePrice.getTimestamp(), 1d, 1d, 1d, 1d));
        }
        this.cashData = cashData;
    }

    public StockData getStockPrice(String symbol) {
        return getStockPrice(symbol, StockConfigBuilder.Interval.ONE_DAY);
    }

    public StockData getStockPrice(String symbol, StockConfigBuilder.Interval interval) {

        if (symbol.equals(Asset.CASH) && cashData != null) {
            return cashData;
        }

        return getStockPrice(
                StockConfigBuilder.builder()
                        .symbol(symbol.equals(Asset.CASH) ? "SPY" : symbol)
                        .fromPeriod(0L)
                        .toPeriod(DateCalc.now())
                        .interval(interval)
                        .build()
        );
    }

    public StockData getStockPrice(StockConfigBuilder builder) {
        String url = builder.build();
        if (stockDataCache.containsKey(url)) {
            log.debug("Fetching stock data from cache: {}", url);
            return stockDataCache.get(url);
        }
        log.debug("Fetching stock data from {}", url);

        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject(url, String.class);

        ObjectMapper mapper = new ObjectMapper();
        StockPrice stockPrice = null;
        try {
            stockPrice = mapper.readValue(result, StockPrice.class);
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching stock data", e);
        }
        StockData stockData = StockData.fromStockPrice(stockPrice);
        stockDataCache.put(url, stockData);
        return stockData;
    }

    public DatePrice getStockPriceAtTime(String symbol, Long timestamp) {
        if (Asset.CASH.equals(symbol)) {
            return new DatePrice(timestamp, 1d, 1d, 1d, 1d);
        }
        StockData stockData = getStockPrice(symbol);
        return findDatePrice(timestamp, stockData);
    }

    public static DatePrice findDatePrice(Long timestamp, StockData stockData) {
        List<DatePrice> datePrices = stockData.getDatePrices();
        DatePrice result = null;
        for (DatePrice datePrice : datePrices) {
            if (datePrice.getTimestamp() <= timestamp) {
                result = datePrice;
            }
        }
        if (result == null || result.getClose() == 0) {
            throw new IllegalArgumentException("No data available for timestamp " + timestamp);
        }
        return result;
    }

}
