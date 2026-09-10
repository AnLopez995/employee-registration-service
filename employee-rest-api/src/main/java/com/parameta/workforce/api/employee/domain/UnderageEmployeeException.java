package com.parameta.workforce.api.employee.domain;

public class UnderageEmployeeException extends RuntimeException {

    public UnderageEmployeeException(String documentNumber) {
        super("Employee with document " + documentNumber + " is not of legal age");
    }
}