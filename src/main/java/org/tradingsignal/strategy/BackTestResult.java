package org.tradingsignal.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tradingsignal.strategy.portfolio.Portfolio;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;

@Data
public class BackTestResult {
    List<TimeValue> portfolioValues;
    Portfolio finalPortfolio;

    public BackTestResult(Portfolio portfolio) {
        this.finalPortfolio = portfolio;
        this.portfolioValues = new LinkedList<>();
    }

    public void addPortfolioValue(Long timestamp, BigDecimal value) {
        this.portfolioValues.add(new TimeValue(timestamp, value.doubleValue()));
    }
}
