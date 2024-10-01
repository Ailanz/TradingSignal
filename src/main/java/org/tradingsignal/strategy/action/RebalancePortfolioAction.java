package org.tradingsignal.strategy.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

@Data
public class RebalancePortfolioAction implements StrategyAction {

    private StockDataService stockDataService;

    private Map<String, Double> targetWeights;

    public RebalancePortfolioAction() {
        targetWeights = new HashMap<>();
        stockDataService = new StockDataService();
    }

    public RebalancePortfolioAction addWeight(String symbol, Double weight) {
        targetWeights.put(symbol.toUpperCase(), weight);
        return this;
    }

    @Override
public Portfolio execute(Portfolio portfolio, Long timestamp, ActionLog actionLog) {
    // Check total weight equals 100
    if (!validateWeights()) {
        throw new RuntimeException("Total weight must be 100");
    }

    // Calculate current weights
    double totalPortfolioValue = portfolio.getPortfolioValue(timestamp);
    Map<String, Double> currentWeights = new HashMap<>();
    for (Map.Entry<String, Asset> entry : portfolio.getAssets().entrySet()) {
        String symbol = entry.getKey();
        Asset asset = entry.getValue();
        double assetValue = asset.getValue(stockDataService, timestamp);
        currentWeights.put(symbol, (assetValue / totalPortfolioValue) * 100);
    }

    // Check if rebalancing is needed
    boolean needsRebalancing = false;
    for (Map.Entry<String, Double> entry : targetWeights.entrySet()) {
        String symbol = entry.getKey();
        double targetWeight = entry.getValue();
        double currentWeight = currentWeights.getOrDefault(symbol, 0.0);
        if (Math.abs(targetWeight - currentWeight) > 1.0) { // Allow a 1% tolerance
            needsRebalancing = true;
            break;
        }
    }

    if (!needsRebalancing) {
        actionLog.addAction(timestamp, "No rebalancing needed. Portfolio already close to target weights.");
        return portfolio;
    }

    // Proceed with rebalancing
    portfolio.sellAll(timestamp);
    totalPortfolioValue = portfolio.getPortfolioValue(timestamp);
    for (Map.Entry<String, Double> entry : targetWeights.entrySet()) {
        String symbol = entry.getKey();
        double targetWeight = entry.getValue();
        double targetValue = totalPortfolioValue * targetWeight / 100;
        double currentSharePrice = stockDataService.getStockPriceAtTime(symbol, timestamp).getClose();
        currentSharePrice = Math.floor(currentSharePrice * 100) / 100;
        double numShares = targetValue / currentSharePrice;
        portfolio.buy(symbol, currentSharePrice, numShares);
    }

    actionLog.addAction(timestamp, "Rebalanced portfolio value " + portfolio.getPortfolioValue(timestamp) + " to target weights " + targetWeights.toString());
    return portfolio;
}

    private boolean validateWeights() {
        return targetWeights.values().stream().mapToDouble(Double::doubleValue).sum() == 100;
    }
}
