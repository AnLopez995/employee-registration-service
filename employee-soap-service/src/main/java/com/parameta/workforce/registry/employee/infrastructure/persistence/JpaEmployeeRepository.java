package com.parameta.workforce.registry.employee.infrastructure.persistence;

import com.parameta.workforce.registry.employee.domain.Employee;
import com.parameta.workforce.registry.employee.domain.EmployeeRepository;
import com.parameta.workforce.registry.employee.domain.exception.EmployeeAlreadyExistsException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaEmployeeRepository implements EmployeeRepository {

    private final EmployeeJpaRepository jpaRepository;

    public JpaEmployeeRepository(EmployeeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public long save(Employee employee) {
        EmployeeEntity entity = new EmployeeEntity(
                employee.firstName(),
                employee.lastName(),
                employee.documentType(),
                employee.documentNumber(),
                employee.birthDate(),
                employee.hireDate(),
                employee.position(),
                employee.salary());
        try {
            return jpaRepository.saveAndFlush(entity).getId();
        } catch (DataIntegrityViolationException ex) {
            throw new EmployeeAlreadyExistsException(
                    employee.documentType(), employee.documentNumber());
        }
    }
}
