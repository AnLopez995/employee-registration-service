package com.parameta.workforce.api.employee.domain.exception;

public class RegistryTimeoutException extends RegistryFailureException {
    public RegistryTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}