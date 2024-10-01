package org.tradingsignal.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockConfigBuilder;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.pojo.yahoo.StockPrice;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.util.DateCalc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Log4j2
@Service
public class StockDataService {

    private static final HashMap<String, StockData> stockDataCache = new HashMap<>();

    public StockData getStockPrice(String symbol) {
        return getStockPrice(
                StockConfigBuilder.builder()
                        .symbol(symbol)
                        .fromPeriod(0L)
                        .toPeriod(DateCalc.now())
                        .interval(StockConfigBuilder.Interval.ONE_DAY)
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

    public DatePrice getLatestStockPrice(String symbol) {
        StockData stockData = getStockPrice(StockConfigBuilder.builder()
                .symbol(symbol)
                .fromPeriod(DateCalc.daysBefore(365))
                .toPeriod(DateCalc.now())
                .interval(StockConfigBuilder.Interval.ONE_DAY)
                .build());
        return stockData.getLatestPrice();
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
        if (result == null) {
            throw new IllegalArgumentException("No data available for timestamp " + timestamp);
        }
        return result;
    }

}
