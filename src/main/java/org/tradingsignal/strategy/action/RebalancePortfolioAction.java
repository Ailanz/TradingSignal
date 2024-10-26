package org.tradingsignal.strategy.action;

import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.strategy.PerformanceMetaData;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public Portfolio execute(Portfolio portfolio, Long timestamp, PerformanceMetaData performanceMetaData) {
        // Check total weight equals 100
        if (!validateWeights()) {
            throw new RuntimeException("Total weight must be 100");
        }
//        performanceMetaData.getActionLog().addAction(timestamp, ActionLog.ActionType.DIVIDEND, "Triggered rebalancing action");

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

        performanceMetaData.getActionLog().addAction(timestamp,
                ActionLog.ActionType.REBALANCE,
                String.format("Rebalanced value %s to weights %s",
                        ActionLog.round(portfolio.getPortfolioValue(timestamp)),
                        targetWeights.toString()),
                portfolio.getPortfolioValue(timestamp));

        if (totalPortfolioValue.doubleValue() != portfolio.getPortfolioValue(timestamp).doubleValue()) {
            throw new RuntimeException("Portfolio value mismatch");
        }
        return portfolio;
    }

    @Override
    public Set<String> getSymbols() {
        return targetWeights.keySet();
    }

    private boolean validateWeights() {
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (BigDecimal weight : targetWeights.values()) {
            totalWeight = totalWeight.add(weight);
        }
        return totalWeight.doubleValue() == 100.0;
    }
}
