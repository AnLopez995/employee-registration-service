package com.parameta.workforce.api.employee.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.parameta.workforce.api.employee.domain.*;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterEmployeeUseCaseTest {

    private static final ZoneId BOGOTA = ZoneId.of("America/Bogota");
    private static final Clock NOON = Clock.fixed(Instant.parse("2026-09-10T17:00:00Z"), BOGOTA);

    @Mock
    private EmployeeRegistry employeeRegistry;

    private Employee employeeBornOn(LocalDate birthDate) {
        return new Employee("Andres", "Lopez", DocumentType.CC, "1020304050",
                birthDate, LocalDate.of(2020, 8, 1), "Backend Developer",
                new BigDecimal("8500000.00"));
    }

    @Test
    void registersAnAdultAndReturnsAgeAndTenure() {
        when(employeeRegistry.register(any())).thenReturn(42L);
        RegisterEmployeeUseCase useCase = new RegisterEmployeeUseCase(employeeRegistry, NOON);

        RegistrationResult result = useCase.register(employeeBornOn(LocalDate.of(1995, 3, 15)));

        assertEquals(42L, result.id());
        assertEquals(new ElapsedTime(31, 5, 26), result.age());
        assertEquals(new ElapsedTime(6, 1, 9), result.tenure());
    }

    @Test
    void rejectsAMinorWithoutCallingTheRegistry() {
        RegisterEmployeeUseCase useCase = new RegisterEmployeeUseCase(employeeRegistry, NOON);
        Employee minor = employeeBornOn(LocalDate.of(2010, 1, 1));

        assertThrows(UnderageEmployeeException.class, () -> useCase.register(minor));

        verify(employeeRegistry, never()).register(any());
    }

    @Test
    void resolvesTodayInBogotaNotInUtc() {
        // 2026-09-11 02:00 UTC = 2026-09-10 21:00 en Bogota
        Clock lateNight = Clock.fixed(Instant.parse("2026-09-11T02:00:00Z"), BOGOTA);
        RegisterEmployeeUseCase useCase = new RegisterEmployeeUseCase(employeeRegistry, lateNight);

        Employee turnsEighteenOnSeptember11 = employeeBornOn(LocalDate.of(2008, 9, 11));

        assertThrows(UnderageEmployeeException.class,
                () -> useCase.register(turnsEighteenOnSeptember11));
        verify(employeeRegistry, never()).register(any());
    }
}