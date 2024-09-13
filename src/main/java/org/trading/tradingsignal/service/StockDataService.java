package org.trading.tradingsignal.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.trading.tradingsignal.pojo.StockData;
import org.trading.tradingsignal.pojo.yahoo.StockPrice;

import java.io.IOException;

@Slf4j
@Service
public class StockDataService {
    private static String apiUrl = "https://query1.finance.yahoo.com/v8/finance/chart/${symbol}?symbol=${symbol}&period1=${from_period}&period2=${to_period}&useYfid=true&interval=${interval}&includePrePost=true&events=div%7Csplit%7Cearn&lang=en-CA&region=CA&crumb=Ydr6HTce7B1&corsDomain=ca.finance.yahoo.com";

    @Getter
    public enum Interval {
        ONE_DAY("1d"),
        FIVE_DAYS("5d"),
        ONE_MONTH("1mo"),
        THREE_MONTHS("3mo"),
        SIX_MONTHS("6mo"),
        ONE_YEAR("1y"),
        TWO_YEARS("2y"),
        FIVE_YEARS("5y"),
        TEN_YEARS("10y"),
        YEAR_TO_DATE("ytd"),
        MAX("max");
        private final String interval;

        Interval(String interval) {
            this.interval = interval;
        }

    }

    public StockData getStockPrice(String symbol) {
        return getStockPrice(symbol, 0, System.currentTimeMillis(), Interval.ONE_DAY);
    }

    public StockData getStockPrice(String symbol, long fromPeriod, long toPeriod, Interval interval) {
        String url = apiUrl.replace("${symbol}", symbol)
                .replace("${from_period}", String.valueOf(fromPeriod))
                .replace("${to_period}", String.valueOf(toPeriod))
                .replace("${interval}", interval.getInterval());

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
        return new StockData(stockPrice);
    }

}
