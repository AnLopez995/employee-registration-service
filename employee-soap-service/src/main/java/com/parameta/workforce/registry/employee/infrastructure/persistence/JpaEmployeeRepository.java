package com.parameta.workforce.registry.employee.infrastructure.persistence;

import com.parameta.workforce.registry.employee.domain.Employee;
import com.parameta.workforce.registry.employee.domain.EmployeeRepository;
import com.parameta.workforce.registry.employee.domain.exception.EmployeeAlreadyExistsException;

import java.util.Locale;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
public class JpaEmployeeRepository implements EmployeeRepository {

    private static final String DOCUMENT_UNIQUE_CONSTRAINT = "uk_employee_document";

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
            if (violatesDocumentUniqueness(ex)) {
                throw new EmployeeAlreadyExistsException(
                        employee.documentType(), employee.documentNumber());
            }
            throw ex;
        }
    }

    private static boolean violatesDocumentUniqueness(DataIntegrityViolationException ex) {
        for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                String name = violation.getConstraintName();
                return name != null
                        && name.toLowerCase(Locale.ROOT).contains(DOCUMENT_UNIQUE_CONSTRAINT);
            }
        }
        return false;
    }
}
