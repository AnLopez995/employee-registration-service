package com.parameta.workforce.registry.employee.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeJpaRepository extends JpaRepository<EmployeeEntity, Long> {
}
