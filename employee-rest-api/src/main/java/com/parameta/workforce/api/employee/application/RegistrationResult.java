package com.parameta.workforce.api.employee.application;

import com.parameta.workforce.api.employee.domain.ElapsedTime;
import com.parameta.workforce.api.employee.domain.Employee;

public record RegistrationResult(long id, Employee employee, ElapsedTime age, ElapsedTime tenure) {
}