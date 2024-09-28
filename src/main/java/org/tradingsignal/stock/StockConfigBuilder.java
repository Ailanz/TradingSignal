package org.tradingsignal.stock;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Log4j2
@Builder
public class StockConfigBuilder {
    private static String YAHOO_API_URL = "https://query1.finance.yahoo.com/v8/finance/chart/${symbol}?symbol=${symbol}&period1=${from_period}&period2=${to_period}&useYfid=true&interval=${interval}&includePrePost=true&events=div%7Csplit%7Cearn&lang=en-CA&region=CA&crumb=Ydr6HTce7B1&corsDomain=ca.finance.yahoo.com";

    private String symbol;
    private Long fromPeriod;
    private Long toPeriod;
    private Interval interval;

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

    public String build() {
        if (fromPeriod == null) {
            fromPeriod = 0L;
        }

        if (toPeriod == null) {
            toPeriod = System.currentTimeMillis();
        }

        if (interval == null) {
            interval = Interval.ONE_DAY;
        }

        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        log.info("Building URL for symbol: {}, fromPeriod: {}, toPeriod: {}, interval: {}", symbol, fromPeriod, toPeriod, interval);
        return YAHOO_API_URL.replace("${symbol}", symbol)
                .replace("${from_period}", String.valueOf(fromPeriod))
                .replace("${to_period}", String.valueOf(toPeriod))
                .replace("${interval}", interval.getInterval());
    }
}
