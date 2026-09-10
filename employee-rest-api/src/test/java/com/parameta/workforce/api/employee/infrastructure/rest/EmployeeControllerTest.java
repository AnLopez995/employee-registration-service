package com.parameta.workforce.api.employee.infrastructure.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.parameta.workforce.api.employee.application.RegisterEmployeeUseCase;
import com.parameta.workforce.api.employee.application.RegistrationResult;
import com.parameta.workforce.api.employee.domain.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterEmployeeUseCase registerEmployee;

    private MultiValueMap<String, String> validParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("firstName", "Andres");
        params.add("lastName", "Lopez");
        params.add("documentType", "CC");
        params.add("documentNumber", "1020304050");
        params.add("birthDate", "1995-03-15");
        params.add("hireDate", "2020-08-01");
        params.add("position", "Backend Developer");
        params.add("salary", "8500000.00");
        return params;
    }

    private Employee sampleEmployee() {
        return new Employee("Andres", "Lopez", DocumentType.CC, "1020304050",
                LocalDate.of(1995, 3, 15), LocalDate.of(2020, 8, 1),
                "Backend Developer", new BigDecimal("8500000.00"));
    }

    @Test
    void returnsTheEmployeeWithAgeAndTenure() throws Exception {
        when(registerEmployee.register(any())).thenReturn(new RegistrationResult(
                42L, sampleEmployee(), new ElapsedTime(31, 5, 26), new ElapsedTime(6, 1, 9)));

        mockMvc.perform(get("/api/v1/employees").params(validParams()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.age.years").value(31))
                .andExpect(jsonPath("$.age.months").value(5))
                .andExpect(jsonPath("$.tenure.years").value(6))
                .andExpect(jsonPath("$.salary").value(8500000.00));
    }

    @Test
    void returnsBadRequestWhenTheDateFormatIsInvalid() throws Exception {
        MultiValueMap<String, String> params = validParams();
        params.set("birthDate", "15-03-1995");

        mockMvc.perform(get("/api/v1/employees").params(params))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("birthDate"))
                .andExpect(jsonPath("$.errors[0].message").value("must follow the format YYYY-MM-DD"));
    }

    @Test
    void returnsConflictWhenTheDocumentIsAlreadyRegistered() throws Exception {
        when(registerEmployee.register(any()))
                .thenThrow(new EmployeeAlreadyExistsException(DocumentType.CC, "1020304050"));

        mockMvc.perform(get("/api/v1/employees").params(validParams()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("https://parameta.com/problems/employee-already-exists"));
    }

    @Test
    void returnsUnprocessableEntityWhenTheEmployeeIsUnderage() throws Exception {
        when(registerEmployee.register(any()))
                .thenThrow(new UnderageEmployeeException("1020304050"));

        mockMvc.perform(get("/api/v1/employees").params(validParams()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void returnsServiceUnavailableWhenTheRegistryIsDown() throws Exception {
        when(registerEmployee.register(any()))
                .thenThrow(new RegistryUnavailableException("down", new RuntimeException()));

        mockMvc.perform(get("/api/v1/employees").params(validParams()))
                .andExpect(status().isServiceUnavailable());
    }
}