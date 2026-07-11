package br.com.lojasquare.utils;

import java.util.Date;

import lombok.Data;

@Data
public class DateDuration {

    private final Date before;
    private Date after;
    private long ms;

    public DateDuration() {
        before = new Date();
    }

    public long calculate() {
        after = new Date();
        ms = after.getTime() - before.getTime();
        return ms;
    }
}
