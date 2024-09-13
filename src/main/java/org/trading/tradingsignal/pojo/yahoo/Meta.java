package org.trading.tradingsignal.pojo.yahoo;

import lombok.Data;

import java.util.List;

@Data
public class Meta {
    private String currency;
    private String symbol;
    private String exchangeName;
    private String fullExchangeName;
    private String instrumentType;
    private int firstTradeDate;
    private int regularMarketTime;
    private boolean hasPrePostMarketData;
    private int gmtoffset;
    private String timezone;
    private String exchangeTimezoneName;
    private double regularMarketPrice;
    private double fiftyTwoWeekHigh;
    private double fiftyTwoWeekLow;
    private double regularMarketDayHigh;
    private double regularMarketDayLow;
    private int regularMarketVolume;
    private String longName;
    private String shortName;
    private double chartPreviousClose;
    private int priceHint;
    private CurrentTradingPeriod currentTradingPeriod;
    private String dataGranularity;
    private String range;
    private List<String> validRanges;
}
