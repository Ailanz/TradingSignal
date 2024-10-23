package org.tradingsignal.strategy.portfolio;

import lombok.Data;
import org.tradingsignal.service.StockDataService;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.HashMap;
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

    public BigDecimal getPortfolioValueOfSymbol(Long timestamp, String symbol) {
        return assets.get(symbol).getValue(stockDataService, timestamp);
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
//            throw new IllegalArgumentException("Not enough cash to buy " + quantity + " of " + symbol);
            //Buy as much as we can
            quantity = assets.get(Asset.CASH).getQuantity().divide(price, 2, RoundingMode.HALF_UP);
            totalValue = price.multiply(quantity);
        }
        assets.get(symbol.toUpperCase()).setQuantity(assets.get(symbol.toUpperCase()).getQuantity().add(quantity));
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().subtract(totalValue));
    }

    public void addCash(BigDecimal amount) {
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().add(amount));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Asset> entry : assets.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue().getQuantity().round(new MathContext(5, RoundingMode.HALF_UP)).doubleValue()).append(", ");
        }
        return sb.toString();
    }

    public void sellSymbolByDollarAmount(String symbol, BigDecimal dollarAmount, Long timestamp) {
        BigDecimal pricePerShare = BigDecimal.valueOf(stockDataService.getStockPriceAtTime(symbol, timestamp).getClose());
        BigDecimal quantity = dollarAmount.divide(pricePerShare, 2, RoundingMode.HALF_UP);
        //TODO: Check if we have enough quantity to sell
        if (assets.get(symbol).getQuantity().doubleValue() < quantity.doubleValue()) {
            //sell all
            sellAll(symbol, timestamp);
            return;
        }
        assets.get(symbol).setQuantity(assets.get(symbol).getQuantity().subtract(quantity));
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().add(dollarAmount));
    }

    public void buySymbolDollarAmount(String symbol, BigDecimal dollarAmount, Long timestamp) {
        BigDecimal price = BigDecimal.valueOf(stockDataService.getStockPriceAtTime(symbol, timestamp).getClose());
        BigDecimal quantity = dollarAmount.divide(price, 2, RoundingMode.HALF_UP);
        buy(symbol, price, quantity);
    }

    public void sellAll(String symbol, Long timestamp) {
        BigDecimal totalValue = assets.get(symbol).getValue(stockDataService, timestamp);
        assets.get(symbol).setQuantity(BigDecimal.ZERO);
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().add(totalValue));
    }
}
