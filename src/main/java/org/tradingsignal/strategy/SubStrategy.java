package org.tradingsignal.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tradingsignal.strategy.action.StrategyAction;

import java.util.List;

@Data
@AllArgsConstructor
public class SubStrategy {
    public enum Operation {
        AND, OR
    }
    private Operation operation;
    private List<Condition> conditions;
    private StrategyAction action;
}
