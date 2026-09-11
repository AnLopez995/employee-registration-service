package com.parameta.workforce.api.employee.infrastructure.soap;

import com.parameta.workforce.api.employee.domain.Employee;
import com.parameta.workforce.api.employee.domain.EmployeeRegistry;
import com.parameta.workforce.api.employee.domain.exception.EmployeeAlreadyExistsException;
import com.parameta.workforce.api.employee.domain.exception.InvalidEmployeeDataException;
import com.parameta.workforce.api.employee.domain.exception.RegistryFailureException;
import com.parameta.workforce.api.employee.domain.exception.RegistryTimeoutException;
import com.parameta.workforce.api.employee.domain.exception.RegistryUnavailableException;
import com.parameta.workforce.api.employee.infrastructure.soap.contract.RegisterEmployeeRequest;
import com.parameta.workforce.api.employee.infrastructure.soap.contract.RegisterEmployeeResponse;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.springframework.oxm.XmlMappingException;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.WebServiceClientException;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.SoapFaultDetailElement;
import org.springframework.ws.soap.client.SoapFaultClientException;

@Component
public class SoapEmployeeRegistry implements EmployeeRegistry {

    private static final QName ERROR_CODE = new QName("http://parameta.com/employees/v1", "errorCode");
    private static final String REJECTED = "Employee registry rejected the request";

    private final WebServiceTemplate webServiceTemplate;

    public SoapEmployeeRegistry(WebServiceTemplate employeeWebServiceTemplate) {
        this.webServiceTemplate = employeeWebServiceTemplate;
    }

    @Override
    public long register(Employee employee) {
        try {
            RegisterEmployeeRequest request = EmployeeSoapMapper.toRequest(employee);
            Object payload = webServiceTemplate.marshalSendAndReceive(request);

            if (payload instanceof RegisterEmployeeResponse response) {
                return response.getId();
            }
            throw new RegistryFailureException("Employee registry returned an unexpected payload");

        } catch (SoapFaultClientException ex) {
            throw translateFault(ex, employee);

        } catch (WebServiceIOException ex) {
            throw translateIoFailure(ex);

        } catch (WebServiceClientException ex) {
            throw new RegistryFailureException("Employee registry call failed", ex);

        } catch (XmlMappingException ex) {
            throw new RegistryFailureException("Employee registry payload could not be mapped", ex);
        }
    }

    private RuntimeException translateFault(SoapFaultClientException ex, Employee employee) {
        SoapFault fault = ex.getSoapFault();
        if (fault == null) {
            return new RegistryFailureException(REJECTED, ex);
        }
        return switch (errorCode(fault).orElse("INTERNAL_ERROR")) {
            case "EMPLOYEE_ALREADY_EXISTS" -> new EmployeeAlreadyExistsException(
                    employee.documentType(), employee.documentNumber());
            case "INVALID_EMPLOYEE_DATA" -> new InvalidEmployeeDataException(fault.getFaultStringOrReason());
            default -> new RegistryFailureException(REJECTED, ex);
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

    private Optional<String> errorCode(SoapFault fault) {
        SoapFaultDetail detail = fault.getFaultDetail();
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

    /**
     * Extracts the text content of a fault detail element.
     *
     * <p>External entity resolution is disabled: the payload comes from a remote
     * service, and a crafted DOCTYPE could otherwise read local files or reach
     * internal hosts (XXE).
     */
    private String textOf(Source source) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "text");

            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            return writer.toString().trim();

        } catch (TransformerException | IllegalArgumentException ex) {
            return "";
        }
    }
}
