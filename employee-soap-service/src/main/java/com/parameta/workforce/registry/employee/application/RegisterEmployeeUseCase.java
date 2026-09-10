package com.parameta.workforce.registry.employee.application;

import com.parameta.workforce.registry.employee.domain.Employee;
import com.parameta.workforce.registry.employee.domain.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterEmployeeUseCase {

    private final EmployeeRepository repository;

    public RegisterEmployeeUseCase(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public long register(Employee employee) {
        return repository.save(employee);
    }
}