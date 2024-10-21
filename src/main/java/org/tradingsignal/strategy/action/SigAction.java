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
            this.targetValue = portfolio.getPortfolioValueOfSymbol(timestamp, riskSymbol).doubleValue() * (1 + increasePct);
            return portfolio;
        }

        BigDecimal startingPortfolioValue = portfolio.getPortfolioValue(timestamp);
        BigDecimal currentRiskValue = portfolio.getPortfolioValueOfSymbol(timestamp, riskSymbol);
        BigDecimal targetRiskValue = BigDecimal.valueOf(targetValue);

        if (portfolio.getPortfolioValue(timestamp).compareTo(startingPortfolioValue) != 0) {
            throw new RuntimeException("Portfolio value changed during rebalance");
        }

        BigDecimal differenceRiskValue = targetRiskValue.subtract(currentRiskValue).abs();
        if (currentRiskValue.compareTo(targetRiskValue) < 0) {
            BigDecimal cash = portfolio.getPortfolioValueOfSymbol(timestamp, Asset.CASH);
            BigDecimal amountNeeded = differenceRiskValue.subtract(cash);
            if (amountNeeded.compareTo(BigDecimal.ZERO) > 0) {
                portfolio.sellSymbolByDollarAmount(safeSymbol, amountNeeded, timestamp);
            }
            portfolio.buySymbolDollarAmount(riskSymbol, differenceRiskValue, timestamp);
        } else if (currentRiskValue.compareTo(targetRiskValue) > 0) {
            // Need to sell some riskSymbol
            portfolio.sellSymbolByDollarAmount(riskSymbol, differenceRiskValue, timestamp);
            BigDecimal cash = portfolio.getPortfolioValueOfSymbol(timestamp, Asset.CASH);
            portfolio.buySymbolDollarAmount(safeSymbol, cash, timestamp);
        }
        this.targetValue = targetRiskValue.doubleValue() * (1 + increasePct);

        BigDecimal finalPortfolioValue = portfolio.getPortfolioValue(timestamp);
        BigDecimal differenceValue = finalPortfolioValue.subtract(startingPortfolioValue).abs();
        if (differenceValue.compareTo(BigDecimal.valueOf(1.0)) > 0) {
            throw new RuntimeException("Portfolio value changed during rebalance: " + differenceValue);
        }

        actionLog.addAction(timestamp, String.format("Adjusted portfolio to target risk value: %s, portfolio value %s", targetRiskValue, portfolio.getPortfolioValue(timestamp)));
        return portfolio;
    }



    @Override
    public Set<String> getSymbols() {
        return Set.of(riskSymbol, safeSymbol);
    }
}
