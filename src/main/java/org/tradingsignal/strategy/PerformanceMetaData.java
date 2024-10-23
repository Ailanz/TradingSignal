package org.tradingsignal.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.tradingsignal.strategy.action.ActionLog;
import org.tradingsignal.strategy.portfolio.Portfolio;
import org.tradingsignal.util.DateCalc;

import java.math.BigDecimal;
import java.util.*;

@Data
public class PerformanceMetaData {
    private Portfolio portfolio;
    private BigDecimal initialValue;
    private long startDate;
    private long endDate;
    private ActionLog actionLog;
    private Map<String, Double> dividends;

    @JsonIgnore
    private List<Long> timestamps = new ArrayList<>();

    public PerformanceMetaData(Portfolio portfolio, BigDecimal initialValue, long startDate, long endDate) {
        this.portfolio = portfolio;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initialValue = initialValue;
        this.actionLog = new ActionLog();
        this.dividends = new HashMap<>();
    }

    @JsonProperty("finalPortfolioValue")
    public BigDecimal getFinalPortfolioValue() {
        return ActionLog.round(portfolio.getPortfolioValue(endDate));
    }

    @JsonProperty("startDate")
    public String getStartDate() {
        return DateCalc.toDateString(startDate);
    }

    @JsonProperty("endDate")
    public String getEndDate() {
        return DateCalc.toDateString(endDate);
    }

    @JsonProperty("pnlPct")
    public double getPnlPct() {
        return ActionLog.round(
                portfolio
                        .getPortfolioValue(this.endDate)
                        .subtract(this.initialValue)
                        .divide(this.initialValue)
                        .multiply(BigDecimal.valueOf(100))
        ).doubleValue();
    }

    @JsonProperty("actionLog")
    public List<AbstractMap.SimpleEntry<Long, String>>  getActionLogs() {
        return actionLog.getActionLog();
    }
}
