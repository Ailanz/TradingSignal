package org.tradingsignal.strategy.action;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;

@Data
public class ActionLog {

    private List<LogData> actionLog = new LinkedList<>();

    public enum ActionType{
        BUY, SELL, DIVIDEND, REBALANCE, SIG, PORTFOLIO_VALUE
    }

    public void addAction (Long timestamp, ActionType actionType, String action){
        actionLog.add(new LogData(timestamp, actionType.name(), action, null));
    }

    public void addAction(Long timestamp, ActionType actionType, String action, BigDecimal portfolioValue) {
        actionLog.add(new LogData(timestamp, actionType.name(), action, ActionLog.round(portfolioValue)));
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}

