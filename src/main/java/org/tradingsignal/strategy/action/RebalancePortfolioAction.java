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

@Data
public class RebalancePortfolioAction implements StrategyAction {

    private StockDataService stockDataService;

    private HashMap<String, Double> targetWeights;

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
        //check total weight equals 100
        if (!validateWeights()) {
            throw new RuntimeException("Total weight must be 100");
        }

        double totalPortfolioValue = 0;
        HashMap<String, Double> currentValues = new HashMap<>();
        for (Asset asset : portfolio.getAssets()) {
            double latestPrice = asset.getSymbol().equals(Asset.CASH) ? 1 : stockDataService.getLatestStockPrice(asset.getSymbol()).getClose();
            double currentValue = asset.getQuantity() * latestPrice;
            totalPortfolioValue += currentValue;
            currentValues.put(asset.getSymbol(), currentValue);
        }


        // Adjust portfolio based on target weights
        for (String symbol : targetWeights.keySet()) {
            double targetWeight = targetWeights.get(symbol);
            double targetValue = totalPortfolioValue * (targetWeight / 100);
            double latestPrice = symbol.equals(Asset.CASH) ? 1 : stockDataService.getLatestStockPrice(symbol).getClose();
            double targetQuantity = targetValue / latestPrice;

            // Find the asset in the portfolio and update its quantity
            boolean assetFound = false;
            for (Asset asset : portfolio.getAssets()) {
                if (asset.getSymbol().equals(symbol)) {
                    asset.setQuantity(targetQuantity);
                    assetFound = true;
                    break;
                }
            }

            // If the asset is not found in the portfolio, add it
            if (!assetFound) {
                portfolio.getAssets().add(new Asset(symbol, 0, targetQuantity));
            }
        }


        actionLog.addAction(timestamp, "Rebalanced portfolio value " + portfolio.getPortfolioValue(timestamp) + " to target weights " + targetWeights.toString());
        return portfolio;
    }

    private boolean validateWeights() {
        double totalWeight = 0;
        for (Double weight : targetWeights.values()) {
            totalWeight += weight;
        }
        if (totalWeight != 100) {
            return false;
        }
        return true;
    }
}
