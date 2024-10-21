package org.tradingsignal.strategy;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.tradingsignal.strategy.action.DividendPaymentAction;
import org.tradingsignal.strategy.action.StrategyAction;
import org.tradingsignal.strategy.condition.Condition;

import java.util.List;

@Data
@AllArgsConstructor
public class SubStrategy {
    public enum Operation {
        AND, OR, ALWAYS_TRUE
    }

    public static final SubStrategy DIVIDEND_PAYMENT = new SubStrategy(SubStrategy.Operation.ALWAYS_TRUE, List.of(), new DividendPaymentAction());

    private Operation operation;
    private List<Condition> conditions;
    private StrategyAction action;

}
