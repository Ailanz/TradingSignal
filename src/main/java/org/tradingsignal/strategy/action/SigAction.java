package org.tradingsignal.strategy.action;

import lombok.Data;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.strategy.PerformanceMetaData;
import org.tradingsignal.strategy.portfolio.Asset;
import org.tradingsignal.strategy.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class SigAction implements StrategyAction {

    private StockDataService stockDataService;

    private double increasePct = 0d;
    private double targetValue = 0d;
    private String riskSymbol;
    private String safeSymbol;
    private double riskWeight;

    public SigAction(double increasePct, String riskSymbol, String safeSymbol, double riskWeight) {
        this.increasePct = increasePct;
        this.stockDataService = new StockDataService();
        this.riskSymbol = riskSymbol;
        this.safeSymbol = safeSymbol;
        this.riskWeight = riskWeight;
    }

    @Override
    public Portfolio execute(Portfolio portfolio, Long timestamp, PerformanceMetaData performanceMetaData) {
        StringBuilder sb = new StringBuilder("Sig Action: ");
        if (targetValue == 0d) {
            new RebalancePortfolioAction()
                    .addWeight(riskSymbol, BigDecimal.valueOf(this.riskWeight))
                    .addWeight(safeSymbol, BigDecimal.valueOf(100 - this.riskWeight)).
                    execute(portfolio, timestamp, performanceMetaData);
            this.targetValue = portfolio.getPortfolioValueOfSymbol(timestamp, riskSymbol).doubleValue() * (1 + increasePct);
            return portfolio;
        }

        BigDecimal startingPortfolioValue = portfolio.getPortfolioValue(timestamp);
        BigDecimal currentRiskValue = portfolio.getPortfolioValueOfSymbol(timestamp, riskSymbol);
        BigDecimal targetRiskValue = BigDecimal.valueOf(targetValue);

        if (portfolio.getPortfolioValue(timestamp).compareTo(startingPortfolioValue) != 0) {
            throw new RuntimeException("Portfolio value changed during rebalance");
        }
        sb.append("Target value: ").append(ActionLog.round(targetRiskValue)).append(" Current value: ").append(ActionLog.round(currentRiskValue));
        BigDecimal differenceRiskValue = targetRiskValue.subtract(currentRiskValue).abs();
        if (currentRiskValue.compareTo(targetRiskValue) < 0) {
            BigDecimal cash = portfolio.getPortfolioValueOfSymbol(timestamp, Asset.CASH);
            BigDecimal amountNeeded = differenceRiskValue.subtract(cash);
            if (amountNeeded.compareTo(BigDecimal.ZERO) > 0) {
                portfolio.sellSymbolByDollarAmount(safeSymbol, amountNeeded, timestamp);
            }
            portfolio.buySymbolDollarAmount(riskSymbol, differenceRiskValue, timestamp);
            sb.append(", Bought ").append(riskSymbol).append(" by ").append(ActionLog.round(differenceRiskValue));
        } else if (currentRiskValue.compareTo(targetRiskValue) > 0) {
            // Need to sell some riskSymbol
            portfolio.sellSymbolByDollarAmount(riskSymbol, differenceRiskValue, timestamp);
            BigDecimal cash = portfolio.getPortfolioValueOfSymbol(timestamp, Asset.CASH);
            portfolio.buySymbolDollarAmount(safeSymbol, cash, timestamp);
            sb.append(", Sold ").append(riskSymbol).append(" by ").append(ActionLog.round(differenceRiskValue));
        }
        this.targetValue = targetRiskValue.doubleValue() * (1 + increasePct);

        BigDecimal finalPortfolioValue = portfolio.getPortfolioValue(timestamp);
        BigDecimal differenceValue = finalPortfolioValue.subtract(startingPortfolioValue).abs();
        if (differenceValue.compareTo(BigDecimal.valueOf(1.0)) > 0) {
            throw new RuntimeException("Portfolio value changed during rebalance: " + differenceValue);
        }
        sb.append(", Portfolio value: ").append(ActionLog.round(finalPortfolioValue));
        performanceMetaData.getActionLog().addAction(timestamp, sb.toString());
        return portfolio;
    }


    @Override
    public Set<String> getSymbols() {
        return Set.of(riskSymbol, safeSymbol);
    }
}
