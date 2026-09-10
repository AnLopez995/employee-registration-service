package com.parameta.workforce.api.employee.infrastructure.rest;

import com.parameta.workforce.api.employee.application.RegisterEmployeeUseCase;
import com.parameta.workforce.api.employee.application.RegistrationResult;
import com.parameta.workforce.api.employee.domain.Employee;
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

    /**
     * Registrar un empleado se expone como metodo get porque tecnicamente fue el requerimiento solicitado,
     * En sistemas productivos esto deberia ser un post, ya que get no es una operacion que deberia manipular recursos en el servidor
     * al ser seguro e idempotente y la informacion personal como salario no deberian exponerse por url,
     * ademas de que se guarda en logs de accceso, en el historial del navegador o cache.
     */
    @GetMapping
    public EmployeeResponse register(@Valid RegisterEmployeeRequest request) {
        Employee employee = EmployeeRestMapper.toDomain(request);
        RegistrationResult result = registerEmployee.register(employee);
        return EmployeeRestMapper.toResponse(result);
    }
}