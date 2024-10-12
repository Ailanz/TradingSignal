package org.tradingsignal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.service.StrategyExecutorService;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.BackTestResult;
import org.tradingsignal.strategy.Condition;
import org.tradingsignal.strategy.StrategyBuilder;
import org.tradingsignal.strategy.SubStrategy;
import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.strategy.action.RebalancePortfolioAction;
import org.tradingsignal.strategy.indicator.ExponentialMovingAverageIndicator;
import org.tradingsignal.strategy.indicator.SimpleMovingAverageIndicator;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.DateCalc;
import org.tradingsignal.util.Utils;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
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

        double initialCash = 10000;
        Portfolio portfolio = new Portfolio(initialCash);

        int daysToBackTest = 366 * 10;
        String leverageSymbol = "TQQQ";
        String symbol = "SVXY";
        StockData mvgAvgStock = stockDataService.getStockPrice("QQQ");
        double smoothing = 2;

        StrategyBuilder strategyBuilder = StrategyBuilder.builder()
                .name("Simple Moving Average")
                .description("Buy when the short-term moving average crosses above the long-term moving average")
                .actionLog(actionLog)
                .subStrategies(List.of(
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new Condition(Condition.ConditionType.GREATER_OR_EQUAL, new SimpleMovingAverageIndicator(200), mvgAvgStock, Condition.ValueType.CURRENT_PRICE)
//                                        new Condition(Condition.ConditionType.GREATER_OR_EQUAL, new ExponentialMovingAverageIndicator(200, smoothing), mvgAvgStock, Condition.ValueType.CURRENT_PRICE)
                                ),
                                new RebalancePortfolioAction().addWeight(leverageSymbol, BigDecimal.valueOf(100d))
                        ),
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new Condition(Condition.ConditionType.LESS, new SimpleMovingAverageIndicator(200), mvgAvgStock, Condition.ValueType.CURRENT_PRICE)
//                                        new Condition(Condition.ConditionType.LESS, new ExponentialMovingAverageIndicator(200, smoothing), mvgAvgStock, Condition.ValueType.CURRENT_PRICE)
                                ),
//                                new RebalancePortfolioAction().addWeight(Asset.CASH, BigDecimal.valueOf(100d))
//                                new RebalancePortfolioAction().addWeight(leverageSymbol, BigDecimal.valueOf(100d))
                                new RebalancePortfolioAction().addWeight(symbol, BigDecimal.valueOf(100d))
//                                new RebalancePortfolioAction().addWeight("SPY", BigDecimal.valueOf(100d))
                        )
                        , SubStrategy.DIVIDEND_PAYMENT
                ))
                .build().build();

        BackTestResult backTestResult = strategyExecutorService.executeStrategy(strategyBuilder, portfolio, DateCalc.daysBefore(daysToBackTest), DateCalc.now());
        BigDecimal finalPortfolioValue = portfolio.getPortfolioValue(DateCalc.now());
        //P&L %
        BigDecimal pnl = finalPortfolioValue.divide(BigDecimal.valueOf(initialCash), new MathContext(4, RoundingMode.HALF_UP));
        actionLog.addAction(DateCalc.now(), "Final portfolio value: " + finalPortfolioValue +
                " P&L: " + Utils.roundDownToTwoDecimals(pnl.doubleValue() * 100 - 100) + "%");
        return actionLog.getActionLog().stream().map(action -> DateCalc.toDateString(action.getKey()) + " : " + action.getValue()).toList();
    }

    @GetMapping("/run")
    public BackTestResult backtest(String symbolRiskOn, String symbolRiskOff) {
        ActionLog actionLog = new ActionLog();

        double initialCash = 10000;
        Portfolio portfolio = new Portfolio(initialCash);

        int daysToBackTest = 366 * 1;
        StockData mvgAvgStock = stockDataService.getStockPrice("QQQ");

        StrategyBuilder strategyBuilder = StrategyBuilder.builder()
                .name("Simple Moving Average")
                .description("Buy when the short-term moving average crosses above the long-term moving average")
                .actionLog(actionLog)
                .subStrategies(List.of(
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new Condition(Condition.ConditionType.GREATER_OR_EQUAL, new SimpleMovingAverageIndicator(200), mvgAvgStock, Condition.ValueType.CURRENT_PRICE)
                                ),
                                new RebalancePortfolioAction().addWeight(symbolRiskOn, BigDecimal.valueOf(100d))
                        ),
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new Condition(Condition.ConditionType.LESS, new SimpleMovingAverageIndicator(200), mvgAvgStock, Condition.ValueType.CURRENT_PRICE)
                                ),
                                new RebalancePortfolioAction().addWeight(symbolRiskOff, BigDecimal.valueOf(100d))
                        )
                        , SubStrategy.DIVIDEND_PAYMENT
                ))
                .build().build();

        BackTestResult backTestResult = strategyExecutorService.executeStrategy(strategyBuilder, portfolio, DateCalc.daysBefore(daysToBackTest), DateCalc.now());
        return backTestResult;
    }


    @GetMapping("/spy")

    public List<String> allSPY() {
        ActionLog actionLog = new ActionLog();

        Portfolio portfolio = new Portfolio(10000);

        StockData spyStockData = stockDataService.getStockPrice("SPY");

        StrategyBuilder strategyBuilder = StrategyBuilder.builder()
                .name("Simple Moving Average")
                .description("Buy when the short-term moving average crosses above the long-term moving average")
                .actionLog(actionLog)
                .subStrategies(List.of(
                        new SubStrategy(
                                SubStrategy.Operation.AND,
                                List.of(
                                        new Condition(Condition.ConditionType.LESS, new SimpleMovingAverageIndicator(10), spyStockData, 1d)
                                ),
                                new RebalancePortfolioAction().addWeight("SPY", BigDecimal.valueOf(100d))
                        )))
                .build().build();

        strategyExecutorService.executeStrategy(strategyBuilder, portfolio, DateCalc.daysBefore(5), DateCalc.now());
        actionLog.addAction(DateCalc.now(), "Final portfolio value: " + portfolio.getPortfolioValue(DateCalc.now()));
        return actionLog.getActionLog().stream().map(action -> DateCalc.toDateString(action.getKey()) + " : " + action.getValue()).toList();
    }
}
