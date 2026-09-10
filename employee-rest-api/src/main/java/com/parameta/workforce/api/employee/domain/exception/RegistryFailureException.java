package com.parameta.workforce.api.employee.domain.exception;

public class RegistryFailureException extends RuntimeException {

    public RegistryFailureException(String message) {
        super(message);
    }
    public RegistryFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
