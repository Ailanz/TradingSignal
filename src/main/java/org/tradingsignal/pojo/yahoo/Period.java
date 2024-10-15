package org.tradingsignal.pojo.yahoo;

import lombok.Data;

@Data
public class Period {
    private String timezone;
    private long end;
    private long start;
    private long gmtoffset;
}
