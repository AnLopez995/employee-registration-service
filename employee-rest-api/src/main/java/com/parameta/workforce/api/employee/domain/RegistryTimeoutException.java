package com.parameta.workforce.api.employee.domain;

public class RegistryTimeoutException extends RegistryFailureException {
    public RegistryTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}