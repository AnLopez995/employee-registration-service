package com.parameta.workforce.api.employee.infrastructure.rest;

public record FieldErrorDetail(String field, String message, String rejectedValue) {
}