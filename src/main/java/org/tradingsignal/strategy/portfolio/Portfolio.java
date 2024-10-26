package org.tradingsignal.strategy.portfolio;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.strategy.action.ActionLog;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Data
public class Portfolio {
    @JsonIgnore
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
        return assets.values().stream()
                .map(asset -> asset.getValue(stockDataService, timestamp))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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
        quantity = ActionLog.round(quantity);
        assets.putIfAbsent(symbol.toUpperCase(), new Asset(symbol, price, BigDecimal.ZERO));
        assets.putIfAbsent(Asset.CASH, new Asset(Asset.CASH, BigDecimal.ONE, BigDecimal.ZERO));

        BigDecimal totalValue = price.multiply(quantity);
        if (assets.get(Asset.CASH).getQuantity().compareTo(totalValue) < 0) {
            quantity = assets.get(Asset.CASH).getQuantity().divide(price, 4, RoundingMode.HALF_DOWN);
            totalValue = price.multiply(quantity);
        }
        assets.get(symbol.toUpperCase()).setQuantity(assets.get(symbol.toUpperCase()).getQuantity().add(quantity));
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().subtract(totalValue));
    }

    public void addCash(BigDecimal amount) {
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().add(amount));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        assets.forEach((key, value) -> sb.append(key).append(": ")
                .append(value.getQuantity().setScale(5, RoundingMode.HALF_DOWN)).append(", "));
        return sb.toString();
    }

    public void sellSymbolByDollarAmount(String symbol, BigDecimal dollarAmount, Long timestamp) {
        BigDecimal pricePerShare = BigDecimal.valueOf(stockDataService.getStockPriceAtTime(symbol, timestamp).getClose());
        BigDecimal quantity = dollarAmount.divide(pricePerShare, 4, RoundingMode.HALF_DOWN);
        if (assets.get(symbol).getQuantity().compareTo(quantity) < 0) {
            sellAll(symbol, timestamp);
            return;
        }
        assets.get(symbol).setQuantity(assets.get(symbol).getQuantity().subtract(quantity));
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().add(dollarAmount));
    }

    public void buySymbolDollarAmount(String symbol, BigDecimal dollarAmount, Long timestamp) {
        BigDecimal price = BigDecimal.valueOf(stockDataService.getStockPriceAtTime(symbol, timestamp).getClose());
        BigDecimal quantity = dollarAmount.divide(price, 5, RoundingMode.HALF_DOWN);
        buy(symbol, price, quantity);
    }

    public void sellAll(String symbol, Long timestamp) {
        BigDecimal totalValue = assets.get(symbol).getValue(stockDataService, timestamp);
        assets.get(symbol).setQuantity(BigDecimal.ZERO);
        assets.get(Asset.CASH).setQuantity(assets.get(Asset.CASH).getQuantity().add(totalValue));
    }
}