package com.parameta.workforce.registry.employee.infrastructure.soap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.clientOrSenderFault;
import static org.springframework.ws.test.server.ResponseMatchers.noFault;
import static org.springframework.ws.test.server.ResponseMatchers.payload;

import com.parameta.workforce.registry.employee.application.RegisterEmployeeUseCase;
import com.parameta.workforce.registry.employee.domain.DocumentType;
import com.parameta.workforce.registry.employee.domain.exception.EmployeeAlreadyExistsException;
import javax.xml.transform.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

@SpringBootTest
class EmployeeEndpointTest {

    private static final String VALID_REQUEST = """
            <registerEmployeeRequest xmlns="http://parameta.com/employees/v1">
                <firstName>Andres</firstName>
                <lastName>Lopez</lastName>
                <documentType>CC</documentType>
                <documentNumber>1020304050</documentNumber>
                <birthDate>1995-03-15</birthDate>
                <hireDate>2020-08-01</hireDate>
                <position>Backend Developer</position>
                <salary>8500000.00</salary>
            </registerEmployeeRequest>""";

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private RegisterEmployeeUseCase registerEmployee;

    private MockWebServiceClient client;

    @BeforeEach
    void setUp() {
        client = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    void registersAnEmployeeAndReturnsTheGeneratedId() {
        when(registerEmployee.register(any())).thenReturn(42L);
        Source request = new StringSource(VALID_REQUEST);

        Source expected = new StringSource("""
                <registerEmployeeResponse xmlns="http://parameta.com/employees/v1">
                    <id>42</id>
                    <documentNumber>1020304050</documentNumber>
                </registerEmployeeResponse>""");

        client.sendRequest(withPayload(request))
                .andExpect(noFault())
                .andExpect(payload(expected));
    }

    @Test
    void returnsAClientFaultWhenTheDocumentIsAlreadyRegistered() {
        when(registerEmployee.register(any()))
                .thenThrow(new EmployeeAlreadyExistsException(DocumentType.CC, "1020304050"));

        client.sendRequest(withPayload(new StringSource(VALID_REQUEST)))
                .andExpect(clientOrSenderFault("Employee is already registered"));
    }

    @Test
    void returnsAFaultAndDoesNotReachTheUseCaseWhenThePayloadIsInvalid() {
        String invalidDate = VALID_REQUEST.replace("1995-03-15", "15-03-1995");

        client.sendRequest(withPayload(new StringSource(invalidDate)))
                .andExpect(clientOrSenderFault());

        verify(registerEmployee, never()).register(any());
    }

    @Test
    void returnsAClientFaultWhenTheFirstNameExceedsTheSchemaLimit() {
        String invalidFirstName = VALID_REQUEST.replace("Andres", "A".repeat(101));

        client.sendRequest(withPayload(new StringSource(invalidFirstName)))
                .andExpect(clientOrSenderFault());

        verify(registerEmployee, never()).register(any());
    }

    // @Test
    // void returnsAClientFaultWhenTheDocumentNumberExceedsTheSchemaLimit() {
    // String invalidDocumentNumber = VALID_REQUEST.replace("1020304050",
    // "1".repeat(21));

    // client.sendRequest(withPayload(new StringSource(invalidDocumentNumber)))
    // .andExpect(clientOrSenderFault());

    // verify(registerEmployee, never()).register(any());
    // }
}
