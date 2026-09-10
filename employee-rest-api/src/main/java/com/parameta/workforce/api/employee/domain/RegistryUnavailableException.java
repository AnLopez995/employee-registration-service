package com.parameta.workforce.api.employee.domain;

public class RegistryUnavailableException extends RegistryFailureException {
    public RegistryUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}