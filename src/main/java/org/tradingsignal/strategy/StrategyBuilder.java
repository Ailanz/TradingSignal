package org.tradingsignal.strategy;

import lombok.Builder;
import lombok.Data;
import org.tradingsignal.strategy.action.StrategyAction;

import java.util.List;

@Data
@Builder
public class StrategyBuilder {
    private String name;
    private String description;
    private Operation operation;
    private List<Condition> conditions;
    private StrategyAction action;
    private int priority;
    private boolean enabled;
    private String createdBy;
    private Long createdDate;
    private String lastModifiedBy;
    private Long lastModifiedDate;
    private int version;

    public enum Operation {
        AND, OR
    }

    public StrategyBuilder build() {

        if (operation == null) {
            throw new InvalidStrategyException("Operation is required");
        }

        if (this.createdDate == null) {
            this.createdDate = System.currentTimeMillis();
        }
        this.lastModifiedDate = System.currentTimeMillis();
        return this;
    }


}
