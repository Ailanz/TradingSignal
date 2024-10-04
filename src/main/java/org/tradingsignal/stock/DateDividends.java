package org.tradingsignal.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateDividends {
    private Long timestamp;
    private Double value;
}
