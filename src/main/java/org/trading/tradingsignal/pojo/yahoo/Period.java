package org.trading.tradingsignal.pojo.yahoo;

import lombok.Data;

@Data
public class Period {
    private String timezone;
    private int end;
    private int start;
    private int gmtoffset;
}
