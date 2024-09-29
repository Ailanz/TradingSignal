package org.tradingsignal.strategy;

import lombok.Data;
import org.tradingsignal.pojo.yahoo.StockPrice;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.indicator.TechnicalIndicator;

import java.util.AbstractMap;
import java.util.List;

@Data
public class Condition {
    public enum ConditionType {
        CROSSOVER,
        CROSSUNDER,
        GREATER,
        LESS,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL
    }

    private ConditionType conditionType;
    private TechnicalIndicator technicalIndicator;
    private List<AbstractMap.SimpleEntry<Long, Double>> indicatorValues;
    private Double value;

    private StockData stockData;

    public Condition(ConditionType conditionType, TechnicalIndicator technicalIndicator, StockData stockData, Double value) {
        this.conditionType = conditionType;
        this.technicalIndicator = technicalIndicator;
        this.indicatorValues = technicalIndicator.calculate(stockData);
        this.value = value;
        this.stockData = stockData;
    }

    public boolean isMet(Long timestamp) {
        Double indicatorValue = getIndicatorValue(timestamp);
        if (indicatorValue == null) {
            return false;
        }

        return switch (conditionType) {
            case CROSSOVER -> isCrossed(ConditionType.CROSSOVER, timestamp);
            case CROSSUNDER -> isCrossed(ConditionType.CROSSUNDER, timestamp);
            case GREATER -> indicatorValue > value;
            case LESS -> indicatorValue < value;
            case GREATER_OR_EQUAL -> indicatorValue >= value;
            case LESS_OR_EQUAL -> indicatorValue <= value;
            default -> false;
        };
    }

    public boolean isCrossed(ConditionType conditionType, Long timestamp) {
        Double value = null;
        Double lastValue = null;
        for (AbstractMap.SimpleEntry<Long, Double> entry : indicatorValues) {
            if (value == null || entry.getKey() <= timestamp) {
                lastValue = value;
                value = entry.getValue();
            }
        }

        if (value == null || lastValue == null) {
            return false;
        }
        if (conditionType == ConditionType.CROSSOVER) {
            return lastValue < this.value && value >= this.value;
        } else if (conditionType == ConditionType.CROSSUNDER) {
            return lastValue > this.value && value <= this.value;
        }
        throw new RuntimeException("Invalid condition type");
    }

    public Double getIndicatorValue(Long timestamp) {
        Double value = null;
        for (AbstractMap.SimpleEntry<Long, Double> entry : indicatorValues) {
            if (value == null || entry.getKey() <= timestamp) {
                value = entry.getValue();
            }
        }
        return value;
    }

    public String getSymbol() {
        return stockData.getSymbol().toUpperCase();
    }

}
