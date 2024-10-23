package org.tradingsignal.strategy.condition;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.tradingsignal.service.StockDataService;
import org.tradingsignal.stock.DatePrice;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.strategy.indicator.TechnicalIndicator;

import java.util.AbstractMap;
import java.util.List;

@Data
@NoArgsConstructor
public class Condition {

    public enum ConditionType {
        GREATER,
        LESS,
        GREATER_OR_EQUAL,
        LESS_OR_EQUAL,
        NONE,
    }

    public enum ValueType {
        CURRENT_PRICE,
        CURRENT_TIMESTAMP,
        CUSTOM_VALUE
    }

    private ConditionType conditionType;
    private TechnicalIndicator technicalIndicator;
    private List<AbstractMap.SimpleEntry<Long, Double>> indicatorValues;
    private ValueType valueType;
    private Double value;

    private StockData stockData;

    public Condition(ConditionType conditionType, TechnicalIndicator technicalIndicator, StockData stockData, Double value) {
        this(conditionType, technicalIndicator, stockData, ValueType.CUSTOM_VALUE, value);
    }

    public Condition(ConditionType conditionType, TechnicalIndicator technicalIndicator, StockData stockData, ValueType valueType) {
        this(conditionType, technicalIndicator, stockData, valueType, null);
    }

    public Condition(ConditionType conditionType, TechnicalIndicator technicalIndicator, StockData stockData, ValueType valueType, Double value) {
        this.conditionType = conditionType;
        this.technicalIndicator = technicalIndicator;
        this.indicatorValues = technicalIndicator.calculate(stockData);
        //ensure indicator values are sorted by timestamp
        this.indicatorValues.sort(java.util.Map.Entry.comparingByKey());

        this.valueType = valueType;
        this.value = value;
        this.stockData = stockData;
    }

    public boolean isMet(Long timestamp, ActionLog actionLog) {
        Double indicatorValue = getIndicatorValue(timestamp);
        if (indicatorValue == null) {
            return false;
        }

        double value = getValue(timestamp);
        boolean isTrue = switch (conditionType) {
            case GREATER -> value > indicatorValue;
            case LESS -> value < indicatorValue;
            case GREATER_OR_EQUAL -> value >= indicatorValue;
            case LESS_OR_EQUAL -> value <= indicatorValue;
            default -> false;
        };

        if (isTrue) {
//            actionLog.addAction(timestamp, "Condition: " + conditionType + " " + " " + valueType + " : " + value  + ", " + technicalIndicator.getClass().getSimpleName() + " : " + indicatorValue);
        } else {
//            actionLog.addAction(timestamp, "Condition not met: " + conditionType + " " + " " + valueType + " : " + value + ", " + technicalIndicator.getClass().getSimpleName() + " : " + indicatorValue);

        }

        return isTrue;
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

    public double getValue(Long timestamp) {
        if (this.valueType == ValueType.CUSTOM_VALUE) {
            return this.value;
        }
        if (this.valueType == ValueType.CURRENT_TIMESTAMP) {
            return timestamp;
        }
        if (this.valueType == ValueType.CURRENT_PRICE) {
            DatePrice datePrice = StockDataService.findDatePrice(timestamp, this.stockData);
            return datePrice.getClose();
        }
        throw new RuntimeException("Invalid value type");
    }

    public String getSymbol() {
        return stockData.getSymbol().toUpperCase();
    }

}
