package com.parameta.workforce.registry.employee.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.parameta.workforce.registry.employee.domain.exception.InvalidEmployeeDataException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EmployeeTest {

    private static final LocalDate BIRTH = LocalDate.of(1995, 3, 15);
    private static final LocalDate HIRE = LocalDate.of(2020, 8, 1);

    private Employee employee(String firstName, BigDecimal salary, LocalDate birth, LocalDate hire) {
        return new Employee(firstName, "Lopez", DocumentType.CC, "1020304050",
                birth, hire, "Backend Developer", salary);
    }

    @Test
    void trimsTextFields() {
        Employee employee = employee("  Andres  ", new BigDecimal("100.00"), BIRTH, HIRE);

        assertEquals("Andres", employee.firstName());
    }

    @Test
    void rejectsBlankText() {
        assertThrows(InvalidEmployeeDataException.class,
                () -> employee("   ", new BigDecimal("100.00"), BIRTH, HIRE));
    }

    @Test
    void rejectsSalaryThatIsNotGreaterThanZero() {
        assertThrows(InvalidEmployeeDataException.class,
                () -> employee("Andres", BigDecimal.ZERO, BIRTH, HIRE));
        assertThrows(InvalidEmployeeDataException.class,
                () -> employee("Andres", new BigDecimal("-1.00"), BIRTH, HIRE));
    }

    @Test
    void rejectsAHireDateBeforeTheBirthDate() {
        assertThrows(InvalidEmployeeDataException.class,
                () -> employee("Andres", new BigDecimal("100.00"), HIRE, BIRTH));
    }
}
