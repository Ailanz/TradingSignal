package org.tradingsignal.strategy.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tradingsignal.service.StockDataService;

import java.util.List;

@Data
public class Portfolio {

    private StockDataService stockDataService;
    private List<Asset> assets;

    public Portfolio(List<Asset> assets) {
        this.stockDataService = new StockDataService();
        this.assets = assets;
    }

    public double getPortfolioValue() {
        double portfolioValue = 0;
        for (Asset asset : assets) {
            portfolioValue += asset.getValue(stockDataService);
        }
        return portfolioValue;
    }

    public double getPortfolioValue(Long timestamp) {
        double portfolioValue = 0;
        for (Asset asset : assets) {
            portfolioValue += asset.getValue(stockDataService, timestamp);
        }
        return portfolioValue;
    }
}
