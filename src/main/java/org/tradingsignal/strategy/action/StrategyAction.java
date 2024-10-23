package org.tradingsignal.strategy.action;

import org.tradingsignal.strategy.PerformanceMetaData;
import org.tradingsignal.strategy.portfolio.Portfolio;

import java.util.Set;


public interface StrategyAction {
    public Portfolio execute(Portfolio portfolio, Long timestamp, PerformanceMetaData performanceMetaData);

    public Set<String> getSymbols();
}
