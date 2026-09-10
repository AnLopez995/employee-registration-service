package com.parameta.workforce.api.employee.infrastructure.rest;

import com.parameta.workforce.api.employee.domain.*;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String PROBLEM_BASE = "https://parameta.com/problems/";

    @ExceptionHandler(EmployeeAlreadyExistsException.class)
    ProblemDetail handleAlreadyExists(EmployeeAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "Employee already registered",
                ex.getMessage(), "employee-already-exists");
    }

    @ExceptionHandler(UnderageEmployeeException.class)
    ProblemDetail handleUnderage(UnderageEmployeeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Employee is not of legal age",
                ex.getMessage(), "underage-employee");
    }

    @ExceptionHandler({ InvalidEmployeeDataException.class, IllegalArgumentException.class })
    ProblemDetail handleInvalidData(RuntimeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid employee data",
                ex.getMessage(), "invalid-employee-data");
    }

    @ExceptionHandler(RegistryUnavailableException.class)
    ProblemDetail handleUnavailable(RegistryUnavailableException ex) {
        log.error("Employee registry unreachable", ex);
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Employee registry unavailable",
                "The employee registry service is temporarily unavailable.", "registry-unavailable");
    }

    @ExceptionHandler(RegistryTimeoutException.class)
    ProblemDetail handleTimeout(RegistryTimeoutException ex) {
        log.error("Employee registry timed out", ex);
        return problem(HttpStatus.GATEWAY_TIMEOUT, "Employee registry timeout",
                "The employee registry service did not respond in time.", "registry-timeout");
    }

    @ExceptionHandler(RegistryFailureException.class)
    ProblemDetail handleRegistryFailure(RegistryFailureException ex) {
        log.error("Employee registry failure", ex);
        return problem(HttpStatus.BAD_GATEWAY, "Employee registry error",
                "The employee registry service could not process the request.", "registry-error");
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal error",
                "An unexpected error occurred.", "internal-error");
    }

    private ProblemDetail problem(HttpStatusCode status, String title, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(PROBLEM_BASE + type));
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<FieldErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDetail(
                        error.getField(),
                        messageFor(error),
                        String.valueOf(error.getRejectedValue())))
                .toList();

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed",
                "One or more fields are invalid.", "validation-failed");
        problem.setProperty("errors", errors);

        return handleExceptionInternal(ex, problem, headers, HttpStatus.BAD_REQUEST, request);
    }

    private String messageFor(FieldError error) {
        if (!"typeMismatch".equals(error.getCode())) {
            return error.getDefaultMessage();
        }
        String[] codes = error.getCodes();
        if (codes != null) {
            for (String code : codes) {
                if (code.endsWith("java.time.LocalDate")) {
                    return "must follow the format YYYY-MM-DD";
                }
                if (code.endsWith("DocumentType")) {
                    return "must be one of: CC, CE, TI, PA";
                }
                if (code.endsWith("java.math.BigDecimal")) {
                    return "must be a valid decimal number";
                }
            }
        }
        return "has an invalid format";
    }
}