package org.trading.tradingsignal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.trading.tradingsignal.pojo.StockData;
import org.trading.tradingsignal.pojo.yahoo.StockPrice;
import org.trading.tradingsignal.service.StockDataService;

import java.io.IOException;

@RestController
@RequestMapping("/stock")
public class StockInfoController {

    @Autowired
    private StockDataService stockDataService;

    @GetMapping("/{symbol}")
    public StockData getStockInfo(@PathVariable String symbol) throws IOException {
        StockData stockData = stockDataService.getStockPrice(symbol);
        return stockData;
    }

}
