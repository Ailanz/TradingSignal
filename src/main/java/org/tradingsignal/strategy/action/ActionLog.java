package org.tradingsignal.strategy.action;

import lombok.Data;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;

@Data
public class ActionLog {
    private List<AbstractMap.SimpleEntry<Long, String>> actionLog = new LinkedList<>();
    public void addAction(Long timestamp, String action) {
        actionLog.add(new AbstractMap.SimpleEntry<>(timestamp, action));
    }

    public static BigDecimal round(BigDecimal value) {
        return value.setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
