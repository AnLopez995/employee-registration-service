package com.parameta.workforce.registry.employee.infrastructure.soap;

import com.parameta.workforce.registry.employee.domain.DocumentType;
import com.parameta.workforce.registry.employee.domain.Employee;
import com.parameta.workforce.registry.employee.infrastructure.soap.contract.RegisterEmployeeRequest;

public final class EmployeeSoapMapper {

    private EmployeeSoapMapper() {
    }

    public static Employee toDomain(RegisterEmployeeRequest request) {
        return new Employee(
                request.getFirstName(),
                request.getLastName(),
                DocumentType.valueOf(request.getDocumentType().value()),
                request.getDocumentNumber(),
                request.getBirthDate(),
                request.getHireDate(),
                request.getPosition(),
                request.getSalary());
    }
}