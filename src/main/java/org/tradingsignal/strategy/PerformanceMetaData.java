package org.tradingsignal.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
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

    //TargetValues is used for SigAction only
    private List<TimeValue> sigTargetValues;
    private List<TimeValue> sigCurrentValues;
    private List<TimeValue> sigSafeValues;


    private Map<String, Double> dividends;
    private List<CompareSymbol> compareSymbols;

    @JsonIgnore
    private List<Long> timestamps = new ArrayList<>();

    public PerformanceMetaData(Portfolio portfolio, BigDecimal initialValue) {
        this.portfolio = portfolio;
        this.initialValue = initialValue;
        this.actionLog = new ActionLog();
        this.dividends = new HashMap<>();
        this.compareSymbols = new ArrayList<>();

        this.sigTargetValues = new ArrayList<>();
        this.sigCurrentValues = new ArrayList<>();
        this.sigSafeValues = new ArrayList<>();
    }

    @JsonProperty("finalPortfolioValue")
    public BigDecimal getFinalPortfolioValue() {
        BigDecimal value = portfolio.getPortfolioValue(endDate);
        return ActionLog.round(value);
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
                        .divide(this.initialValue, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
        ).doubleValue();
    }

    @JsonProperty("actionLog")
    public List<AbstractMap.SimpleEntry<Long, String>>  getActionLogs() {
        return actionLog.getActionLog();
    }
}

@Data
@AllArgsConstructor
class CompareSymbol {
    private String symbol;
    private String startDate;
    private double initialPrice;
    private String endDate;
    private double finalPrice;
    private double pnlPct;
}
