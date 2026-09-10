package com.parameta.workforce.api.employee.domain;

public class RegistryFailureException extends RuntimeException {
    public RegistryFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
