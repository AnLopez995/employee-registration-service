package com.parameta.workforce.api.employee.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 9, 10);

    private Employee employee(LocalDate birthDate, LocalDate hireDate) {
        return new Employee("Andres", "Lopez", DocumentType.CC, "1020304050",
                birthDate, hireDate, "Backend Developer", new BigDecimal("8500000.00"));
    }

    @Test
    void calculatesAgeInYearsMonthsAndDays() {
        Employee employee = employee(LocalDate.of(1995, 3, 15), LocalDate.of(2020, 8, 1));

        assertEquals(new ElapsedTime(31, 5, 26), employee.ageAt(TODAY));
    }

    @Test
    void calculatesTenureInYearsMonthsAndDays() {
        Employee employee = employee(LocalDate.of(1995, 3, 15), LocalDate.of(2020, 8, 1));

        assertEquals(new ElapsedTime(6, 1, 9), employee.tenureAt(TODAY));
    }

    @Test
    void rejectsMinorsOnTheDayBeforeTheirEighteenthBirthday() {
        Employee employee = employee(LocalDate.of(2008, 9, 11), LocalDate.of(2026, 1, 1));

        assertFalse(employee.isOfLegalAgeAt(TODAY));
    }

    @Test
    void acceptsAdultsExactlyOnTheirEighteenthBirthday() {
        Employee employee = employee(LocalDate.of(2008, 9, 10), LocalDate.of(2026, 1, 1));

        assertTrue(employee.isOfLegalAgeAt(TODAY));
    }
}