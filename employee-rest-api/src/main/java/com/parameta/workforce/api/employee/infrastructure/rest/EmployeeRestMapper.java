package com.parameta.workforce.api.employee.infrastructure.rest;

import com.parameta.workforce.api.employee.application.RegistrationResult;
import com.parameta.workforce.api.employee.domain.Employee;

public final class EmployeeRestMapper {

    private EmployeeRestMapper() {
    }

    public static Employee toDomain(RegisterEmployeeRequest request) {
        return new Employee(
                request.getFirstName(),
                request.getLastName(),
                request.getDocumentType(),
                request.getDocumentNumber(),
                request.getBirthDate(),
                request.getHireDate(),
                request.getPosition(),
                request.getSalary());
    }

    public static EmployeeResponse toResponse(RegistrationResult result) {
        Employee employee = result.employee();
        return new EmployeeResponse(
                result.id(),
                employee.firstName(),
                employee.lastName(),
                employee.documentType(),
                employee.documentNumber(),
                employee.birthDate(),
                employee.hireDate(),
                employee.position(),
                employee.salary(),
                result.age(),
                result.tenure());
    }
}