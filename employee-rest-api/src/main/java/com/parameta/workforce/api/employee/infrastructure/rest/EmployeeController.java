package com.parameta.workforce.api.employee.infrastructure.rest;

import com.parameta.workforce.api.employee.application.RegisterEmployeeUseCase;
import com.parameta.workforce.api.employee.application.RegistrationResult;
import com.parameta.workforce.api.employee.domain.Employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {

    private final RegisterEmployeeUseCase registerEmployee;

    public EmployeeController(RegisterEmployeeUseCase registerEmployee) {
        this.registerEmployee = registerEmployee;
    }

    @Operation(summary = "Registers an employee and returns age and tenure", description = "Exposed as GET because the technical test requires it. "
            + "In production this would be POST: see docs/DECISIONES.md, entry 7.")
    @ApiResponse(responseCode = "200", description = "Employee registered")
@ApiResponse(responseCode = "400", description = "Invalid format or failed validation", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class), examples = @ExampleObject(value = """
                    {
                      "type": "https://parameta.com/problems/validation-failed",
                      "title": "Validation failed",
                      "status": 400,
                      "detail": "One or more fields are invalid.",
                      "instance": "/api/v1/employees",
                      "errors": [
                        {
                          "field": "birthDate",
                          "message": "must follow the format YYYY-MM-DD",
                          "rejectedValue": "15-03-1995"
                        }
                      ]
                    }""")))
@ApiResponse(responseCode = "409", description = "Document already registered", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "422", description = "Business rule violated (underage, inconsistent dates)", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "502", description = "Employee registry failed", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "503", description = "Employee registry unreachable", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
@ApiResponse(responseCode = "504", description = "Employee registry timed out", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public EmployeeResponse register(@Valid RegisterEmployeeRequest request) {
        Employee employee = EmployeeRestMapper.toDomain(request);
        RegistrationResult result = registerEmployee.register(employee);
        return EmployeeRestMapper.toResponse(result);
    }
}