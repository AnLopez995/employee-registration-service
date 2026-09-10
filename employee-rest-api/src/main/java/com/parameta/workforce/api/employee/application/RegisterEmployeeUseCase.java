package com.parameta.workforce.api.employee.application;

import com.parameta.workforce.api.employee.domain.Employee;
import com.parameta.workforce.api.employee.domain.EmployeeRegistry;
import com.parameta.workforce.api.employee.domain.UnderageEmployeeException;
import java.time.Clock;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class RegisterEmployeeUseCase {

    private final EmployeeRegistry employeeRegistry;
    private final Clock clock;

    public RegisterEmployeeUseCase(EmployeeRegistry employeeRegistry, Clock clock) {
        this.employeeRegistry = employeeRegistry;
        this.clock = clock;
    }

    public RegistrationResult register(Employee employee) {
        LocalDate today = LocalDate.now(clock);

        if (!employee.isOfLegalAgeAt(today)) {
            throw new UnderageEmployeeException(employee.documentNumber());
        }

        long id = employeeRegistry.register(employee);

        return new RegistrationResult(id, employee, employee.ageAt(today), employee.tenureAt(today));
    }
}