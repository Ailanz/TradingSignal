package org.trading.tradingsignal.pojo.yahoo;

import lombok.Data;

@Data
public class CurrentTradingPeriod {
    private Period pre;
    private Period regular;
    private Period post;
}
