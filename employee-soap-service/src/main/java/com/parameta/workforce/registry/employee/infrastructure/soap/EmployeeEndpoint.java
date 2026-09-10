package com.parameta.workforce.registry.employee.infrastructure.soap;

import com.parameta.workforce.registry.employee.application.RegisterEmployeeUseCase;
import com.parameta.workforce.registry.employee.domain.Employee;
import com.parameta.workforce.registry.employee.infrastructure.soap.contract.RegisterEmployeeRequest;
import com.parameta.workforce.registry.employee.infrastructure.soap.contract.RegisterEmployeeResponse;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class EmployeeEndpoint {

    private static final String NAMESPACE = "http://parameta.com/employees/v1";

    private final RegisterEmployeeUseCase registerEmployee;

    public EmployeeEndpoint(RegisterEmployeeUseCase registerEmployee) {
        this.registerEmployee = registerEmployee;
    }

    @PayloadRoot(namespace = NAMESPACE, localPart = "registerEmployeeRequest")
    @ResponsePayload
    public RegisterEmployeeResponse registerEmployee(@RequestPayload RegisterEmployeeRequest request) {
        Employee employee = EmployeeSoapMapper.toDomain(request);
        long id = registerEmployee.register(employee);

        RegisterEmployeeResponse response = new RegisterEmployeeResponse();
        response.setId(id);
        response.setDocumentNumber(employee.documentNumber());
        return response;
    }
}