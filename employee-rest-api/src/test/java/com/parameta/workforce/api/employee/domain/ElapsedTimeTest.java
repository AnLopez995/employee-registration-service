package com.parameta.workforce.api.employee.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.parameta.workforce.api.employee.domain.exception.InvalidEmployeeDataException;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.Test;

class ElapsedTimeTest {

    @Test
    void isZeroWhenStartAndEndAreTheSameDay() {
        LocalDate day = LocalDate.of(2026, Month.SEPTEMBER, 10);

        assertEquals(new ElapsedTime(0, 0, 0), ElapsedTime.between(day, day));
    }

    @Test
    void countsWholeYearsMonthsAndDays() {
        ElapsedTime elapsed = ElapsedTime.between(
                LocalDate.of(2020, Month.AUGUST, 1), LocalDate.of(2026, Month.SEPTEMBER, 10));

        assertEquals(new ElapsedTime(6, 1, 9), elapsed);
    }

    @Test
    void aPersonBornOnFebruary29TurnsAgeOnMarch1InNonLeapYears() {
        LocalDate leapDayBirth = LocalDate.of(2000, Month.FEBRUARY, 29);

        assertEquals(25, ElapsedTime.between(leapDayBirth, LocalDate.of(2026, Month.FEBRUARY, 28)).years());
        assertEquals(26, ElapsedTime.between(leapDayBirth, LocalDate.of(2026, Month.MARCH, 1)).years());
    }

    @Test
    void rejectsAnEndDateBeforeTheStartDate() {
        LocalDate start = LocalDate.of(2026, Month.JANUARY, 1);
        LocalDate end = LocalDate.of(2025, Month.JANUARY, 1);

        assertThrows(InvalidEmployeeDataException.class, () -> ElapsedTime.between(start, end));
    }
}