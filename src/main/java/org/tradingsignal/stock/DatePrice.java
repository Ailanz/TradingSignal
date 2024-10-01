package org.tradingsignal.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatePrice {
    private Long timestamp;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
}
