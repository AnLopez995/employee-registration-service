package com.parameta.workforce.api.employee.infrastructure.soap;

import com.parameta.workforce.api.employee.domain.*;
import com.parameta.workforce.api.employee.infrastructure.soap.contract.RegisterEmployeeRequest;
import com.parameta.workforce.api.employee.infrastructure.soap.contract.RegisterEmployeeResponse;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Optional;
import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.client.SoapFaultClientException;

@Component
public class SoapEmployeeRegistry implements EmployeeRegistry {

    private static final QName ERROR_CODE = new QName("http://parameta.com/employees/v1", "errorCode");

    private final WebServiceTemplate webServiceTemplate;

    public SoapEmployeeRegistry(WebServiceTemplate employeeWebServiceTemplate) {
        this.webServiceTemplate = employeeWebServiceTemplate;
    }

    @Override
    public long register(Employee employee) {
        try {
            RegisterEmployeeRequest request = EmployeeSoapMapper.toRequest(employee);
            RegisterEmployeeResponse response = (RegisterEmployeeResponse) webServiceTemplate
                    .marshalSendAndReceive(request);
            return response.getId();

        } catch (SoapFaultClientException ex) {
            throw translateFault(ex, employee);

        } catch (WebServiceIOException ex) {
            throw translateIoFailure(ex);

        } catch (WebServiceClientException ex) {
            throw new RegistryFailureException("Employee registry call failed", ex);
        }
    }

    private RuntimeException translateFault(SoapFaultClientException ex, Employee employee) {
        return switch (errorCode(ex).orElse("INTERNAL_ERROR")) {
            case "EMPLOYEE_ALREADY_EXISTS" -> new EmployeeAlreadyExistsException(
                    employee.documentType(), employee.documentNumber());
            case "INVALID_EMPLOYEE_DATA" -> new InvalidEmployeeDataException(
                    ex.getSoapFault().getFaultStringOrReason());
            default -> new RegistryFailureException(
                    "Employee registry rejected the request", ex);
        };
    }

    private RuntimeException translateIoFailure(WebServiceIOException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof SocketTimeoutException) {
            return new RegistryTimeoutException("Employee registry did not respond in time", ex);
        }
        if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
            return new RegistryUnavailableException("Employee registry is unreachable", ex);
        }
        return new RegistryFailureException("Employee registry communication failed", ex);
    }

    private Optional<String> errorCode(SoapFaultClientException ex) {
        SoapFaultDetail detail = ex.getSoapFault().getFaultDetail();
        if (detail == null) {
            return Optional.empty();
        }
        Iterator<SoapFaultDetailElement> entries = detail.getDetailEntries();
        while (entries.hasNext()) {
            SoapFaultDetailElement element = entries.next();
            if (ERROR_CODE.equals(element.getName())) {
                return Optional.of(textOf(element.getSource()));
            }
        }
        return Optional.empty();
    }

    private String textOf(Source source) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "text");
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            return writer.toString().trim();
        } catch (TransformerException ex) {
            return "";
        }
    }
}