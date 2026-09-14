package com.parameta.workforce.api.employee.infrastructure.rest;

import com.parameta.workforce.api.employee.domain.DocumentType;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@Data
public class RegisterEmployeeRequest {

        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must be at most 100 characters")
        String firstName;

        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must be at most 100 characters")
        String lastName;

        @NotNull(message = "is required")
        DocumentType documentType;

        @NotBlank(message = "must not be blank")
        @Size(max = 20, message = "must be at most 20 characters")
        String documentNumber;

        @NotNull(message = "is required")
        @Past(message = "must be a date in the past")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate birthDate;

        @NotNull(message = "is required")
        @PastOrPresent(message = "must not be a future date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate hireDate;

        @NotBlank(message = "must not be blank")
        @Size(max = 100, message = "must be at most 100 characters")
        String position;

        @NotNull(message = "is required")
        @Positive(message = "must be greater than zero")
        @Digits(integer = 13, fraction = 2, message = "must not have more than 13 integer digits and 2 decimal places")
        BigDecimal salary;
}
