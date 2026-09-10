package com.parameta.workforce.api.employee.domain.exception;

import com.parameta.workforce.api.employee.domain.DocumentType;

public class EmployeeAlreadyExistsException extends RuntimeException {
    public EmployeeAlreadyExistsException(DocumentType documentType, String documentNumber) {
        super("Employee with document " + documentType + "-" + documentNumber
                + " is already registered");
    }
}