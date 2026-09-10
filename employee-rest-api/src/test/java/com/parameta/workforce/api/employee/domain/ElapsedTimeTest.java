package com.parameta.workforce.api.employee.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ElapsedTimeTest {

    @Test
    void isZeroWhenStartAndEndAreTheSameDay() {
        LocalDate day = LocalDate.of(2026, 9, 10);

        assertEquals(new ElapsedTime(0, 0, 0), ElapsedTime.between(day, day));
    }

    @Test
    void countsWholeYearsMonthsAndDays() {
        ElapsedTime elapsed = ElapsedTime.between(
                LocalDate.of(2020, 8, 1), LocalDate.of(2026, 9, 10));

        assertEquals(new ElapsedTime(6, 1, 9), elapsed);
    }

    @Test
    void aPersonBornOnFebruary29TurnsAgeOnMarch1InNonLeapYears() {
        LocalDate leapDayBirth = LocalDate.of(2000, 2, 29);

        assertEquals(25, ElapsedTime.between(leapDayBirth, LocalDate.of(2026, 2, 28)).years());
        assertEquals(26, ElapsedTime.between(leapDayBirth, LocalDate.of(2026, 3, 1)).years());
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDate() {
        assertThrows(IllegalArgumentException.class,
                () -> ElapsedTime.between(LocalDate.of(2026, 1, 1), LocalDate.of(2025, 1, 1)));
    }
}