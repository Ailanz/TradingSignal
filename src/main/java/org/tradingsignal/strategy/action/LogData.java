package org.tradingsignal.strategy.action;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LogData {
    private Long timestamp;
    private String action;
    private String message;
    private BigDecimal portfolioValue;
}
