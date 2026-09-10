package com.parameta.workforce.api.employee.domain;

import java.time.LocalDate;
import java.time.Period;

public record ElapsedTime(int years, int months, int days) {

    public static ElapsedTime between(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end dates are required");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end date cannot be before start date");
        }
        Period period = Period.between(start, end);
        return new ElapsedTime(period.getYears(), period.getMonths(), period.getDays());
    }
}