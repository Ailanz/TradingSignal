package org.tradingsignal.strategy.action;

import org.tradingsignal.strategy.portfolio.Portfolio;


public interface StrategyAction {
    public Portfolio execute(Portfolio portfolio, Long timestamp, ActionLog actionLog);
}
