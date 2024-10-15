package org.tradingsignal.strategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Data
public class TimeValue {
    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("America/New_York"));

    private String time;
    @JsonIgnore
    private Long timestamp;
    private double value;

    public TimeValue(Long timestamp, double value) {
        //format timestamp to yyyy-MM-dd
        this.time = formatter.format(Instant.ofEpochMilli(timestamp*1000));
        this.timestamp = timestamp;
        this.value = value;
    }
}
