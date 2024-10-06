package org.tradingsignal.strategy.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.Utils;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Data
public class RebalancePortfolioAction implements StrategyAction {

    private StockDataService stockDataService;

    private Map<String, BigDecimal> targetWeights;

    public RebalancePortfolioAction() {
        targetWeights = new HashMap<>();
        stockDataService = new StockDataService();
    }

    public RebalancePortfolioAction addWeight(String symbol, BigDecimal weight) {
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
        BigDecimal totalPortfolioValue = portfolio.getPortfolioValue(timestamp);
        Map<String, BigDecimal> currentWeights = new HashMap<>();
        for (Map.Entry<String, Asset> entry : portfolio.getAssets().entrySet()) {
            String symbol = entry.getKey();
            Asset asset = entry.getValue();
            BigDecimal assetValue = asset.getValue(stockDataService, timestamp);
            currentWeights.put(symbol, assetValue.divide(totalPortfolioValue, 2, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100)));
        }

        // Check if rebalancing is needed
        boolean needsRebalancing = false;
        for (Map.Entry<String, BigDecimal> entry : targetWeights.entrySet()) {
            String symbol = entry.getKey();
            BigDecimal targetWeight = entry.getValue();
            BigDecimal currentWeight = currentWeights.getOrDefault(symbol, BigDecimal.valueOf(0));
            if (Math.abs(targetWeight.doubleValue() - currentWeight.doubleValue()) > 1.0) { // Allow a 1% tolerance
                needsRebalancing = true;
                break;
            }
        }

        if (!needsRebalancing) {
//            actionLog.addAction(timestamp, "No rebalancing needed. Portfolio already close to target weights.");
            return portfolio;
        }

        // Proceed with rebalancing
        portfolio.sellAll(timestamp);
        for (Map.Entry<String, BigDecimal> entry : targetWeights.entrySet()) {
            String symbol = entry.getKey();
            BigDecimal targetWeight = entry.getValue();
            BigDecimal targetValue = totalPortfolioValue.multiply(targetWeight).divide(BigDecimal.valueOf(100));
            BigDecimal currentSharePrice = BigDecimal.valueOf(stockDataService.getStockPriceAtTime(symbol, timestamp).getClose());
            BigDecimal numShares = targetValue.divide(currentSharePrice, 2, BigDecimal.ROUND_DOWN);
            portfolio.buy(symbol, currentSharePrice, numShares);
        }

        actionLog.addAction(timestamp,String.format("Rebalanced value %s to weights %s", Utils.roundDownToTwoDecimals(portfolio.getPortfolioValue(timestamp).doubleValue()), targetWeights.toString()));

        if (totalPortfolioValue.doubleValue() != portfolio.getPortfolioValue(timestamp).doubleValue()) {
            throw new RuntimeException("Portfolio value mismatch");
        }
        return portfolio;
    }

    private boolean validateWeights() {
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (BigDecimal weight : targetWeights.values()) {
            totalWeight = totalWeight.add(weight);
        }
        return totalWeight.doubleValue() == 100.0;
    }
}
