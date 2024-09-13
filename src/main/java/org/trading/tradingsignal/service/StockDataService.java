package org.trading.tradingsignal.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.trading.tradingsignal.pojo.stock.StockConfigBuilder;
import org.trading.tradingsignal.pojo.stock.StockData;
import org.trading.tradingsignal.pojo.yahoo.StockPrice;

import java.io.IOException;

@Slf4j
@Service
public class StockDataService {

    public StockData getStockPrice(String symbol) {
        return getStockPrice(
                StockConfigBuilder.builder()
                        .symbol(symbol)
                        .fromPeriod(0L)
                        .toPeriod(System.currentTimeMillis())
                        .interval(StockConfigBuilder.Interval.ONE_DAY)
                        .build()
        );
    }

    public StockData getStockPrice(StockConfigBuilder builder) {
        String url = builder.build();
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
        return StockData.fromStockPrice(stockPrice);
    }

}
