package org.tradingsignal.pojo.yahoo;

import lombok.Data;

import java.util.List;

@Data
public class Chart {
    private List<Result> result;
    private Object error;
}
