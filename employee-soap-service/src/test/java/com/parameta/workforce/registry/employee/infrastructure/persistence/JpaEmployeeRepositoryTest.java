package com.parameta.workforce.registry.employee.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.parameta.workforce.registry.employee.domain.DocumentType;
import com.parameta.workforce.registry.employee.domain.Employee;
import com.parameta.workforce.registry.employee.domain.exception.EmployeeAlreadyExistsException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaEmployeeRepository.class)
class JpaEmployeeRepositoryTest {

    @Autowired
    private JpaEmployeeRepository repository;

    private Employee employee(DocumentType documentType, String documentNumber) {
        return new Employee("Andres", "Lopez", documentType, documentNumber,
                LocalDate.of(1995, 3, 15), LocalDate.of(2020, 8, 1),
                "Backend Developer", new BigDecimal("8500000.00"));
    }

    @Test
    void savesAnEmployeeAndReturnsTheGeneratedId() {
        long id = repository.save(employee(DocumentType.CC, "1020304050"));

        assertTrue(id > 0);
    }

    @Test
    void rejectsTheSameDocumentTwice() {
        repository.save(employee(DocumentType.CC, "1020304050"));

        EmployeeAlreadyExistsException ex = assertThrows(EmployeeAlreadyExistsException.class,
                () -> repository.save(employee(DocumentType.CC, "1020304050")));

        assertEquals("Employee with document CC-1020304050 is already registered", ex.getMessage());
    }

    @Test
    void allowsTheSameNumberWithADifferentDocumentType() {
        long first = repository.save(employee(DocumentType.CC, "1020304050"));
        long second = repository.save(employee(DocumentType.TI, "1020304050"));

        assertNotEquals(first, second);
    }

    @Test
    void preservesTheSalaryScale() {
        long id = repository.save(employee(DocumentType.CE, "999888777"));

        assertTrue(id > 0);
    }
}
