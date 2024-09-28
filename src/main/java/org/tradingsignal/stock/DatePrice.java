package org.tradingsignal.stock;

import lombok.Data;

@Data
public class DatePrice {
    private Integer timestamp;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
}
