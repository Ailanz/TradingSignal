package org.tradingsignal.strategy.action;

import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.strategy.PerformanceMetaData;
import org.tradingsignal.strategy.TimeValue;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class SigAction implements StrategyAction {

    private StockDataService stockDataService = new StockDataService();
    private double increasePct;
    private double targetValue = 0d;
    private String riskSymbol;
    private String safeSymbol;
    private double riskWeight;

    public SigAction(double increasePct, String riskSymbol, String safeSymbol, double riskWeight) {
        this.increasePct = increasePct;
        this.riskSymbol = riskSymbol;
        this.safeSymbol = safeSymbol;
        this.riskWeight = riskWeight;
    }

    @Override
    public Portfolio execute(Portfolio portfolio, Long timestamp, PerformanceMetaData performanceMetaData) {
        if (targetValue == 0d) {
            new RebalancePortfolioAction()
                    .addWeight(riskSymbol, BigDecimal.valueOf(riskWeight))
                    .addWeight(safeSymbol, BigDecimal.valueOf(100 - riskWeight))
                    .execute(portfolio, timestamp, performanceMetaData);
            targetValue = portfolio.getPortfolioValueOfSymbol(timestamp, riskSymbol).doubleValue() * (1 + increasePct);
            return portfolio;
        }

        BigDecimal startingPortfolioValue = portfolio.getPortfolioValue(timestamp);
        BigDecimal currentRiskValue = portfolio.getPortfolioValueOfSymbol(timestamp, riskSymbol);
        BigDecimal currentSafeValue = portfolio.getPortfolioValueOfSymbol(timestamp, safeSymbol);
        BigDecimal targetRiskValue = BigDecimal.valueOf(targetValue);

        performanceMetaData.getSigTargetValues().add(new TimeValue(timestamp, targetRiskValue.doubleValue()));
        performanceMetaData.getSigCurrentValues().add(new TimeValue(timestamp, currentRiskValue.doubleValue()));
        performanceMetaData.getSigSafeValues().add(new TimeValue(timestamp, currentSafeValue.doubleValue()));

        if (!portfolio.getPortfolioValue(timestamp).equals(startingPortfolioValue)) {
            throw new RuntimeException("Portfolio value changed during rebalance");
        }

        BigDecimal differenceRiskValue = targetRiskValue.subtract(currentRiskValue).abs();
        StringBuilder sb = new StringBuilder("Target value: $")
                .append(ActionLog.round(targetRiskValue))
                .append(" Current value: $")
                .append(ActionLog.round(currentRiskValue));

        if (currentRiskValue.compareTo(targetRiskValue) < 0) {
            BigDecimal cash = portfolio.getPortfolioValueOfSymbol(timestamp, Asset.CASH);
            BigDecimal amountNeeded = differenceRiskValue.subtract(cash);
            if (amountNeeded.compareTo(BigDecimal.ZERO) > 0) {
                portfolio.sellSymbolByDollarAmount(safeSymbol, amountNeeded, timestamp);
            }
            cash = portfolio.getPortfolioValueOfSymbol(timestamp, Asset.CASH);
            portfolio.buySymbolDollarAmount(riskSymbol, differenceRiskValue, timestamp);
            BigDecimal actualBought = cash.min(differenceRiskValue);
            sb.append(", Bought ").append(riskSymbol).append(" by $").append(ActionLog.round(actualBought)).append(" from ").append(safeSymbol);
        } else if (currentRiskValue.compareTo(targetRiskValue) > 0) {
            portfolio.sellSymbolByDollarAmount(riskSymbol, differenceRiskValue, timestamp);
            BigDecimal cash = portfolio.getPortfolioValueOfSymbol(timestamp, Asset.CASH);
            portfolio.buySymbolDollarAmount(safeSymbol, cash, timestamp);
            sb.append(", Sold ").append(riskSymbol).append(" by $").append(ActionLog.round(differenceRiskValue)).append(" to ").append(safeSymbol);
        }

        targetValue = targetRiskValue.doubleValue() * (1 + increasePct);

        BigDecimal finalPortfolioValue = portfolio.getPortfolioValue(timestamp);
        BigDecimal differenceValue = finalPortfolioValue.subtract(startingPortfolioValue).abs();
        if (differenceValue.compareTo(BigDecimal.valueOf(1.0)) > 0) {
            throw new RuntimeException("Portfolio value changed during rebalance: $" + differenceValue);
        }

        performanceMetaData.getActionLog().addAction(timestamp, ActionLog.ActionType.SIG, sb.toString(), ActionLog.round(finalPortfolioValue));
        return portfolio;
    }

    @Override
    public Set<String> getSymbols() {
        return Set.of(riskSymbol, safeSymbol);
    }
}