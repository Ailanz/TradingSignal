package org.tradingsignal.strategy.operation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.tradingsignal.stock.StockData;
import org.tradingsignal.strategy.Condition;
import org.tradingsignal.strategy.indicator.TechnicalIndicator;

import java.util.AbstractMap;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ConditionTest {
    private Condition condition;
    private TechnicalIndicator technicalIndicator;
    private StockData stockData;

    @BeforeEach
    void setUp() {
        technicalIndicator = Mockito.mock(TechnicalIndicator.class);
        stockData = Mockito.mock(StockData.class);
    }

    @Test
    void conditionIsMetWhenCrossOver() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 5.0),
                new AbstractMap.SimpleEntry<>(2L, 6.0)
        ));
        condition = new Condition(Condition.ConditionType.CROSSOVER, technicalIndicator, stockData, 5.5);
        assertTrue(condition.isMet(2L));
    }

    @Test
    void conditionIsNotMetWhenCrossOver() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 6.0),
                new AbstractMap.SimpleEntry<>(2L, 5.0)
        ));
        condition = new Condition(Condition.ConditionType.CROSSOVER, technicalIndicator, stockData, 5.5);
        assertFalse(condition.isMet(2L));
    }

    @Test
    void conditionIsMetWhenCrossUnder() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 6.0),
                new AbstractMap.SimpleEntry<>(2L, 5.0)
        ));
        condition = new Condition(Condition.ConditionType.CROSSUNDER, technicalIndicator, stockData, 5.5);
        assertTrue(condition.isMet(2L));
    }

    @Test
    void conditionIsNotMetWhenCrossUnder() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 5.0),
                new AbstractMap.SimpleEntry<>(2L, 6.0)
        ));
        condition = new Condition(Condition.ConditionType.CROSSUNDER, technicalIndicator, stockData, 5.5);
        assertFalse(condition.isMet(2L));
    }

    @Test
    void conditionIsMetWhenGreater() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 6.0)
        ));
        condition = new Condition(Condition.ConditionType.GREATER, technicalIndicator, stockData, 5.5);
        assertTrue(condition.isMet(1L));
    }

    @Test
    void conditionIsNotMetWhenGreater() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 5.0)
        ));
        condition = new Condition(Condition.ConditionType.GREATER, technicalIndicator, stockData, 5.5);
        assertFalse(condition.isMet(1L));
    }

    @Test
    void conditionIsMetWhenLess() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 5.0)
        ));
        condition = new Condition(Condition.ConditionType.LESS, technicalIndicator, stockData, 5.5);
        assertTrue(condition.isMet(1L));
    }

    @Test
    void conditionIsNotMetWhenLess() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 6.0)
        ));
        condition = new Condition(Condition.ConditionType.LESS, technicalIndicator, stockData, 5.5);
        assertFalse(condition.isMet(1L));
    }

    @Test
    void conditionIsMetWhenGreaterOrEqual() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 5.5)
        ));
        condition = new Condition(Condition.ConditionType.GREATER_OR_EQUAL, technicalIndicator, stockData, 5.5);
        assertTrue(condition.isMet(1L));
    }

    @Test
    void conditionIsNotMetWhenGreaterOrEqual() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 5.0)
        ));
        condition = new Condition(Condition.ConditionType.GREATER_OR_EQUAL, technicalIndicator, stockData, 5.5);
        assertFalse(condition.isMet(1L));
    }

    @Test
    void conditionIsMetWhenLessOrEqual() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 5.5)
        ));
        condition = new Condition(Condition.ConditionType.LESS_OR_EQUAL, technicalIndicator, stockData, 5.5);
        assertTrue(condition.isMet(1L));
    }

    @Test
    void conditionIsNotMetWhenLessOrEqual() {
        Mockito.when(technicalIndicator.calculate(stockData)).thenReturn(Arrays.asList(
                new AbstractMap.SimpleEntry<>(1L, 6.0)
        ));
        condition = new Condition(Condition.ConditionType.LESS_OR_EQUAL, technicalIndicator, stockData, 5.5);
        assertFalse(condition.isMet(1L));
    }
}