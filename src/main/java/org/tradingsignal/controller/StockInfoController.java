package org.tradingsignal.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.stock.StockConfigBuilder;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.util.DateCalc;

import java.io.IOException;

@RestController
@RequestMapping("/stock")
@Log4j2
public class StockInfoController {

    @PostConstruct
    public void init()
    {
        log.info("StockInfoController initialized");
    }

    @Autowired
    private StockDataService stockDataService;

    @GetMapping("/{symbol}")
    public StockData getStockInfo(@PathVariable String symbol) throws IOException {
        log.info("Fetching stock info for symbol: {}", symbol);
        StockData stockData = stockDataService.getStockPrice(
                StockConfigBuilder
                        .builder()
                        .fromPeriod(DateCalc.daysBefore(12))
                        .toPeriod(DateCalc.now())
                        .symbol(symbol)
                        .build());
        return stockData;
    }

}
