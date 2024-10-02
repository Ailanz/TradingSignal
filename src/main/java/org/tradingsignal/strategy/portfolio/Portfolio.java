package org.tradingsignal.strategy.portfolio;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.util.Utils;

import java.math.BigDecimal;
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
        this();
        this.assets.put(Asset.CASH, new Asset(Asset.CASH, BigDecimal.ONE, BigDecimal.valueOf(cash)));
    }

    public BigDecimal getPortfolioValue(Long timestamp) {
        BigDecimal portfolioValue = BigDecimal.ZERO;
        for (Asset asset : assets.values()) {
            portfolioValue = portfolioValue.add(asset.getValue(stockDataService, timestamp));
        }
        return portfolioValue;
    }

    public void sellAll(Long timestamp) {
        BigDecimal totalPortfolioValue = getPortfolioValue(timestamp);
        assets.clear();
        assets.put(Asset.CASH, new Asset(Asset.CASH, BigDecimal.ONE, totalPortfolioValue));
    }

    public void buy(String symbol, BigDecimal price, BigDecimal quantity) {
        assets.putIfAbsent(symbol.toUpperCase(), new Asset(symbol, price, BigDecimal.ZERO));
        assets.putIfAbsent(Asset.CASH, new Asset(Asset.CASH, BigDecimal.ONE, BigDecimal.ZERO));

        BigDecimal totalValue = price.multiply(quantity);
        if (assets.get(Asset.CASH).getQuantity().doubleValue() < totalValue.doubleValue()) {
            throw new IllegalArgumentException("Not enough cash to buy " + quantity + " of " + symbol);
        }
        assets.get(symbol.toUpperCase()).setQuantity(assets.get(symbol.toUpperCase()).getQuantity().add(quantity));
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().subtract(totalValue));
    }
}
