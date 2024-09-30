package org.tradingsignal.strategy.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tradingsignal.service.StockDataService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Portfolio {
    //TODO: DUMB THIS DOWN, REMOVE STOCKDATA SERVICE
    private StockDataService stockDataService;
    private Map<String, Asset> assets;

    public Portfolio() {
        this.stockDataService = new StockDataService();
        this.assets = new HashMap<>();
    }

    public Portfolio(double cash) {
        this.stockDataService = new StockDataService();
        this.assets = new HashMap<>();
        this.assets.put(Asset.CASH, new Asset(Asset.CASH, 1, cash));
    }

    public double getPortfolioValue() {
        double portfolioValue = 0;
        for (Asset asset : assets.values()) {
            portfolioValue += asset.getValue(stockDataService);
        }
        return portfolioValue;
    }

    public double getPortfolioValue(Long timestamp) {
        double portfolioValue = 0;
        for (Asset asset : assets.values()) {
            portfolioValue += asset.getValue(stockDataService, timestamp);
        }
        return portfolioValue;
    }

    public void sellAll(Long timestamp) {
        double totalPortfolioValue = getPortfolioValue(timestamp);
        assets.clear();
        assets.put(Asset.CASH, new Asset(Asset.CASH, 1, totalPortfolioValue));
    }

    public void buy(String symbol, double price, double quantity) {
        assets.putIfAbsent(symbol.toUpperCase(), new Asset(symbol, price, 0));
        assets.putIfAbsent(Asset.CASH, new Asset(Asset.CASH, 1, 0));

        //round price to 2 decimal places
        price = Math.round(price * 100.0) / 100.0;
        quantity = Math.round(quantity * 100.0) / 100.0;
        double totalValue = price * quantity;
        if (assets.get(Asset.CASH).getQuantity() < totalValue) {
            throw new IllegalArgumentException("Not enough cash to buy " + quantity + " of " + symbol);
        }
        assets.get(symbol.toUpperCase()).setQuantity(assets.get(symbol.toUpperCase()).getQuantity() + quantity);
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity() - totalValue);
    }
}
