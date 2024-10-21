package org.tradingsignal.strategy.condition;

import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.util.DateCalc;

public class PeriodicTimeCondition extends Condition {

    private final int days;
    private long nextDate = 0;

    public PeriodicTimeCondition(int days) {
        super();
        this.days = days;
    }

    @Override
    public boolean isMet(Long timestamp, ActionLog actionLog) {
        if (timestamp > nextDate) {
            nextDate = DateCalc.daysAfter(timestamp, days);
//            actionLog.addAction(timestamp, "PeriodicTimeCondition: " + days + " days passed");
            return true;
        }
        return false;
    }

    @Override
    public String getSymbol() {
        return null;
    }
}
