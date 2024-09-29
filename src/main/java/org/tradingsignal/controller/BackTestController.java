package org.tradingsignal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.service.StrategyExecutorService;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.Condition;
import org.tradingsignal.strategy.StrategyBuilder;
import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.strategy.action.RebalancePortfolioAction;
import org.tradingsignal.strategy.indicator.SimpleMovingAverageIndicator;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.DateCalc;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/back-test")

public class BackTestController {

    @Autowired
    private StrategyExecutorService strategyExecutorService;

    @Autowired
    private StockDataService stockDataService;

    @GetMapping("/test")
    public List<String> test() {
        ActionLog actionLog = new ActionLog();

        List<Asset> assets = new LinkedList();
        assets.add(new Asset(Asset.CASH, 1, 10000));
        Portfolio portfolio = new Portfolio(assets);

        StockData spyStockData = stockDataService.getStockPrice("SPY");

        StrategyBuilder strategyBuilder = StrategyBuilder.builder()
                .name("Simple Moving Average")
                .description("Buy when the short-term moving average crosses above the long-term moving average")
                .operation(StrategyBuilder.Operation.AND)
                .conditions(List.of(new Condition(Condition.ConditionType.GREATER, new SimpleMovingAverageIndicator(50), spyStockData, 100d)))
                .action(new RebalancePortfolioAction().addWeight("SPY", 100d))
                .build();

        strategyExecutorService.executeStrategy(strategyBuilder, portfolio, actionLog, DateCalc.daysBefore(365), DateCalc.now());
        return actionLog.getActionLog().stream().map(action -> action.getKey() + " : " + action.getValue()).toList();
    }
}
