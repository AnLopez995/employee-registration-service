package com.parameta.workforce.registry.employee.infrastructure.persistence;

import com.parameta.workforce.registry.employee.domain.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_employee_document",
               columnNames = {"document_type", "document_number"}))
public class EmployeeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 2)
    private DocumentType documentType;

    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false, length = 100)
    private String position;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salary;

    protected EmployeeEntity() {
    }

    public EmployeeEntity(String firstName, String lastName, DocumentType documentType,
                          String documentNumber, LocalDate birthDate, LocalDate hireDate,
                          String position, BigDecimal salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.birthDate = birthDate;
        this.hireDate = hireDate;
        this.position = position;
        this.salary = salary;
    }

    public Long getId() {
        return id;
    }
}
