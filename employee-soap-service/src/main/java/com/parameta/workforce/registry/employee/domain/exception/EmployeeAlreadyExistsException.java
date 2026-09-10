package com.parameta.workforce.registry.employee.domain.exception;

import com.parameta.workforce.registry.employee.domain.DocumentType;

public class EmployeeAlreadyExistsException extends RuntimeException {

    public EmployeeAlreadyExistsException(DocumentType documentType, String documentNumber) {
        super("Employee with document " + documentType + "-" + documentNumber
                + " is already registered");
    }
}