package br.com.lojasquare.utils;

import lombok.Data;

import java.util.Date;

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
        return ms = after.getTime() - before.getTime();
    }

}
