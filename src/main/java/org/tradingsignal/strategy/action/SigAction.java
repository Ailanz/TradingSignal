package org.tradingsignal.strategy.action;

import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class SigAction implements StrategyAction {

    private StockDataService stockDataService;

    private double increasePct = 0d;
    private double targetValue = 0d;
    private String riskSymbol;
    private String safeSymbol;

    public SigAction(double increasePct, String riskSymbol, String safeSymbol) {
        this.increasePct = increasePct;
        this.stockDataService = new StockDataService();
        this.riskSymbol = riskSymbol;
        this.safeSymbol = safeSymbol;

    }

    @Override
    public Portfolio execute(Portfolio portfolio, Long timestamp, ActionLog actionLog) {
        if (targetValue == 0d) {
            new RebalancePortfolioAction()
                    .addWeight(riskSymbol, BigDecimal.valueOf(60))
                    .addWeight(safeSymbol, BigDecimal.valueOf(40)).
                    execute(portfolio, timestamp, new ActionLog());
            this.targetValue = portfolio.getPortfolioValueOfSymbol(timestamp, riskSymbol).doubleValue();
        }

        BigDecimal currentRiskValue = portfolio.getPortfolioValueOfSymbol(timestamp, riskSymbol);
        BigDecimal targetRiskValue = BigDecimal.valueOf(targetValue);

        if (currentRiskValue.compareTo(targetRiskValue) < 0) {
            // Need to buy more riskSymbol
            rebalanceToTarget(portfolio, timestamp, currentRiskValue, targetRiskValue, safeSymbol, riskSymbol);
        } else if (currentRiskValue.compareTo(targetRiskValue) > 0) {
            // Need to sell some riskSymbol
            rebalanceToTarget(portfolio, timestamp, targetRiskValue, currentRiskValue, riskSymbol, safeSymbol);
        }
        this.targetValue = targetRiskValue.doubleValue() * (1 + increasePct);
        actionLog.addAction(timestamp, String.format("Adjusted portfolio to target risk value: %s", targetRiskValue));
        return portfolio;
    }

    private void rebalanceToTarget(Portfolio portfolio, Long timestamp, BigDecimal currentRiskValue, BigDecimal targetRiskValue, String safeSymbol, String riskSymbol) {
        BigDecimal amountToBuy = targetRiskValue.subtract(currentRiskValue);
        BigDecimal safeSymbolPrice = BigDecimal.valueOf(stockDataService.getStockPriceAtTime(safeSymbol, timestamp).getClose());
        BigDecimal riskSymbolPrice = BigDecimal.valueOf(stockDataService.getStockPriceAtTime(riskSymbol, timestamp).getClose());

        BigDecimal safeSymbolQuantityToSell = amountToBuy.divide(safeSymbolPrice, 2, BigDecimal.ROUND_DOWN);
        portfolio.sell(safeSymbol, safeSymbolPrice, safeSymbolQuantityToSell, timestamp);
        portfolio.buy(riskSymbol, riskSymbolPrice, amountToBuy.divide(riskSymbolPrice, 2, BigDecimal.ROUND_DOWN));
    }

    @Override
    public Set<String> getSymbols() {
        return Set.of(riskSymbol, safeSymbol);
    }
}
