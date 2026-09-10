package com.parameta.workforce.api.employee.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.parameta.workforce.api.employee.domain.exception.InvalidEmployeeDataException;

public record Employee(
        String firstName,
        String lastName,
        DocumentType documentType,
        String documentNumber,
        LocalDate birthDate,
        LocalDate hireDate,
        String position,
        BigDecimal salary) {
    private static final int LEGAL_AGE = 18;

    public Employee {
        firstName = requireText(firstName, "firstName");
        lastName = requireText(lastName, "lastName");
        documentNumber = requireText(documentNumber, "documentNumber");
        position = requireText(position, "position");

        if (documentType == null) {
            throw new InvalidEmployeeDataException("documentType is required");
        }
        if (birthDate == null) {
            throw new InvalidEmployeeDataException("birthDate is required");
        }
        if (hireDate == null) {
            throw new InvalidEmployeeDataException("hireDate is required");
        }
        if (salary == null || salary.signum() <= 0) {
            throw new InvalidEmployeeDataException("salary must be greater than zero");
        }
        if (hireDate.isBefore(birthDate)) {
            throw new InvalidEmployeeDataException("hireDate cannot be before birthDate");
        }
    }

    public ElapsedTime ageAt(LocalDate referenceDate) {
        return ElapsedTime.between(birthDate, referenceDate);
    }

    public ElapsedTime tenureAt(LocalDate referenceDate) {
        return ElapsedTime.between(hireDate, referenceDate);
    }

    public boolean isOfLegalAgeAt(LocalDate referenceDate) {
        return ageAt(referenceDate).years() >= LEGAL_AGE;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidEmployeeDataException(field + " is required");
        }
        return value.trim();
    }
}