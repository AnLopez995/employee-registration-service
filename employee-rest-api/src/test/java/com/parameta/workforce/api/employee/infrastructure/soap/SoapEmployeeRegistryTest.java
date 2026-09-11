package com.parameta.workforce.api.employee.infrastructure.soap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.ws.test.client.RequestMatchers.anything;
import static org.springframework.ws.test.client.RequestMatchers.payload;
import static org.springframework.ws.test.client.ResponseCreators.withError;
import static org.springframework.ws.test.client.ResponseCreators.withException;
import static org.springframework.ws.test.client.ResponseCreators.withPayload;
import static org.springframework.ws.test.client.ResponseCreators.withSoapEnvelope;

import com.parameta.workforce.api.config.SoapClientConfig;
import com.parameta.workforce.api.employee.domain.DocumentType;
import com.parameta.workforce.api.employee.domain.Employee;
import com.parameta.workforce.api.employee.domain.exception.EmployeeAlreadyExistsException;
import com.parameta.workforce.api.employee.domain.exception.InvalidEmployeeDataException;
import com.parameta.workforce.api.employee.domain.exception.RegistryFailureException;
import com.parameta.workforce.api.employee.domain.exception.RegistryTimeoutException;
import com.parameta.workforce.api.employee.domain.exception.RegistryUnavailableException;
import jakarta.xml.soap.MessageFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;
import org.springframework.ws.soap.client.SoapFaultClientException;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.test.client.MockWebServiceServer;
import org.springframework.xml.transform.StringSource;

class SoapEmployeeRegistryTest {

    private static final Employee EMPLOYEE = new Employee("Andres", "Lopez", DocumentType.CC, "1020304050",
            LocalDate.of(1995, Month.MARCH, 15), LocalDate.of(2020, Month.AUGUST, 1),
            "Backend Developer", new BigDecimal("8500000.00"));

    private MockWebServiceServer server;
    private SoapEmployeeRegistry registry;

    @BeforeEach
    void setUp() throws Exception {
        SoapClientConfig config = new SoapClientConfig();
        Jaxb2Marshaller marshaller = config.employeeMarshaller();
        marshaller.afterPropertiesSet();
        WebServiceTemplate template = config.employeeWebServiceTemplate(
                marshaller, "http://localhost:8081/ws", Duration.ofSeconds(1), Duration.ofSeconds(1));

        server = MockWebServiceServer.createServer(template);
        registry = new SoapEmployeeRegistry(template);
    }

    @AfterEach
    void everyExpectedCallWasMade() {
        server.verify();
    }

    @Test
    void sendsTheEmployeeAsTheContractDefinesAndReturnsTheGeneratedId() {
        server.expect(payload(new StringSource("""
                        <registerEmployeeRequest xmlns="http://parameta.com/employees/v1">
                            <firstName>Andres</firstName>
                            <lastName>Lopez</lastName>
                            <documentType>CC</documentType>
                            <documentNumber>1020304050</documentNumber>
                            <birthDate>1995-03-15</birthDate>
                            <hireDate>2020-08-01</hireDate>
                            <position>Backend Developer</position>
                            <salary>8500000.00</salary>
                        </registerEmployeeRequest>""")))
                .andRespond(withPayload(new StringSource("""
                        <registerEmployeeResponse xmlns="http://parameta.com/employees/v1">
                            <id>42</id>
                        </registerEmployeeResponse>""")));

        assertEquals(42L, registry.register(EMPLOYEE));
    }

    @Test
    void translatesADuplicateDocumentFault() {
        server.expect(anything()).andRespond(withSoapEnvelope(
                faultEnvelope("Employee is already registered", errorCodeDetail("EMPLOYEE_ALREADY_EXISTS"))));

        EmployeeAlreadyExistsException ex = assertThrows(EmployeeAlreadyExistsException.class,
                () -> registry.register(EMPLOYEE));

        assertEquals("Employee with document CC-1020304050 is already registered", ex.getMessage());
    }

    @Test
    void translatesAnInvalidDataFaultKeepingTheFaultReason() {
        server.expect(anything()).andRespond(withSoapEnvelope(
                faultEnvelope("Invalid employee data", errorCodeDetail("INVALID_EMPLOYEE_DATA"))));

        InvalidEmployeeDataException ex = assertThrows(InvalidEmployeeDataException.class,
                () -> registry.register(EMPLOYEE));

        assertEquals("Invalid employee data", ex.getMessage());
    }

    @Test
    void treatsAFaultWithoutDetailAsARegistryFailure() {
        server.expect(anything()).andRespond(withSoapEnvelope(faultEnvelope("Internal service error", "")));

        assertGenericRegistryFailure();
    }

    @Test
    void treatsAFaultWithAnUnknownDetailAsARegistryFailure() {
        server.expect(anything()).andRespond(withSoapEnvelope(faultEnvelope("Something else",
                "<detail><reason xmlns=\"http://parameta.com/employees/v1\">other</reason></detail>")));

        assertGenericRegistryFailure();
    }

    @Test
    void treatsAFaultExceptionWithoutFaultElementAsARegistryFailure() throws Exception {
        SoapMessage messageWithoutFault =
                new SaajSoapMessageFactory(MessageFactory.newInstance()).createWebServiceMessage();
        WebServiceTemplate template = mock(WebServiceTemplate.class);
        when(template.marshalSendAndReceive(any())).thenThrow(new SoapFaultClientException(messageWithoutFault));
        SoapEmployeeRegistry registryWithBrokenFault = new SoapEmployeeRegistry(template);

        RegistryFailureException ex = assertThrows(RegistryFailureException.class,
                () -> registryWithBrokenFault.register(EMPLOYEE));

        assertEquals(RegistryFailureException.class, ex.getClass());
    }

    @Test
    void rejectsAResponseWithoutPayload() {
        server.expect(anything()).andRespond(withSoapEnvelope(new StringSource("""
                <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                    <SOAP-ENV:Body/>
                </SOAP-ENV:Envelope>""")));

        assertGenericRegistryFailure();
    }

    @Test
    void rejectsAResponseTheContractDoesNotDefine() {
        server.expect(anything()).andRespond(withPayload(new StringSource(
                "<unexpectedResponse xmlns=\"http://parameta.com/employees/v1\"/>")));

        assertGenericRegistryFailure();
    }

    @Test
    void mapsARefusedConnectionToRegistryUnavailable() {
        server.expect(anything()).andRespond(withException(new ConnectException("Connection refused")));

        assertThrows(RegistryUnavailableException.class, () -> registry.register(EMPLOYEE));
    }

    @Test
    void mapsAnUnknownHostToRegistryUnavailable() {
        server.expect(anything()).andRespond(withException(new UnknownHostException("employee-soap-service")));

        assertThrows(RegistryUnavailableException.class, () -> registry.register(EMPLOYEE));
    }

    @Test
    void mapsATimeoutToRegistryTimeout() {
        server.expect(anything()).andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThrows(RegistryTimeoutException.class, () -> registry.register(EMPLOYEE));
    }

    @Test
    void mapsAnyOtherIoFailureToRegistryFailure() {
        server.expect(anything()).andRespond(withException(new IOException("Broken pipe")));

        assertGenericRegistryFailure();
    }

    @Test
    void mapsATransportErrorToRegistryFailure() {
        server.expect(anything()).andRespond(withError("Service Unavailable"));

        assertGenericRegistryFailure();
    }

    @Test
    void mapsAnyOtherClientFailureToRegistryFailure() {
        server.expect(anything()).andRespond(withException(
                new WebServiceClientException("Unexpected client failure") {
                }));

        assertGenericRegistryFailure();
    }

    private void assertGenericRegistryFailure() {
        RegistryFailureException ex = assertThrows(RegistryFailureException.class,
                () -> registry.register(EMPLOYEE));

        assertEquals(RegistryFailureException.class, ex.getClass(),
                "expected the generic registry failure, not a more specific subtype");
    }

    private static StringSource faultEnvelope(String faultString, String detail) {
        return new StringSource("""
                <SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
                    <SOAP-ENV:Body>
                        <SOAP-ENV:Fault>
                            <faultcode>SOAP-ENV:Client</faultcode>
                            <faultstring>%s</faultstring>
                            %s
                        </SOAP-ENV:Fault>
                    </SOAP-ENV:Body>
                </SOAP-ENV:Envelope>""".formatted(faultString, detail));
    }

    private static String errorCodeDetail(String code) {
        return "<detail><errorCode xmlns=\"http://parameta.com/employees/v1\">" + code + "</errorCode></detail>";
    }
}
