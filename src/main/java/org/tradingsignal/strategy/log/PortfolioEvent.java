package org.tradingsignal.strategy.log;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PortfolioEvent {
    enum EventType {
        REBALANCE, DIVIDEND, BUY, SELL, NONE
    }

    private EventType eventType;
    private List<String> symbols;
    private String message;
}
