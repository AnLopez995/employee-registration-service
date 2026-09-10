package com.parameta.workforce.api.employee.infrastructure.rest;

import com.parameta.workforce.api.employee.application.RegisterEmployeeUseCase;
import com.parameta.workforce.api.employee.application.RegistrationResult;
import com.parameta.workforce.api.employee.domain.Employee;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee registered"),
            @ApiResponse(responseCode = "400", description = "Invalid format or failed validation"),
            @ApiResponse(responseCode = "409", description = "Document already registered"),
            @ApiResponse(responseCode = "422", description = "Business rule violated"),
            @ApiResponse(responseCode = "503", description = "Employee registry unreachable"),
            @ApiResponse(responseCode = "504", description = "Employee registry timed out")
    })
    @GetMapping
    public EmployeeResponse register(@Valid RegisterEmployeeRequest request) {
        Employee employee = EmployeeRestMapper.toDomain(request);
        RegistrationResult result = registerEmployee.register(employee);
        return EmployeeRestMapper.toResponse(result);
    }
}