package com.parameta.workforce.api.employee.infrastructure.rest;

import com.parameta.workforce.api.employee.domain.DocumentType;
import com.parameta.workforce.api.employee.domain.ElapsedTime;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmployeeResponse(
        long id,
        String firstName,
        String lastName,
        DocumentType documentType,
        String documentNumber,
        LocalDate birthDate,
        LocalDate hireDate,
        String position,
        BigDecimal salary,
        ElapsedTime age,
        ElapsedTime tenure) {
}