package com.parameta.workforce.api.employee.domain.exception;

public class UnderageEmployeeException extends RuntimeException {

    public UnderageEmployeeException(String documentNumber) {
        super("Employee with document " + documentNumber + " is not of legal age");
    }
}