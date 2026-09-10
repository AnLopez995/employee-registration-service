package com.parameta.workforce.api.employee.infrastructure.soap;

import com.parameta.workforce.api.employee.domain.Employee;
import com.parameta.workforce.api.employee.infrastructure.soap.contract.DocumentType;
import com.parameta.workforce.api.employee.infrastructure.soap.contract.RegisterEmployeeRequest;

public final class EmployeeSoapMapper {

    private EmployeeSoapMapper() {
    }

    public static RegisterEmployeeRequest toRequest(Employee employee) {
        RegisterEmployeeRequest request = new RegisterEmployeeRequest();
        request.setFirstName(employee.firstName());
        request.setLastName(employee.lastName());
        request.setDocumentType(DocumentType.fromValue(employee.documentType().name()));
        request.setDocumentNumber(employee.documentNumber());
        request.setBirthDate(employee.birthDate());
        request.setHireDate(employee.hireDate());
        request.setPosition(employee.position());
        request.setSalary(employee.salary());
        return request;
    }
}