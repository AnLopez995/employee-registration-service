package com.parameta.workforce.registry.config;

import com.parameta.workforce.registry.employee.domain.exception.EmployeeAlreadyExistsException;
import com.parameta.workforce.registry.employee.domain.exception.InvalidEmployeeDataException;

import java.util.Properties;
import javax.xml.namespace.QName;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.SoapFault;
import org.springframework.ws.soap.SoapFaultDetail;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;
import org.springframework.ws.soap.server.endpoint.SoapFaultMappingExceptionResolver;

@Component
public class SoapExceptionResolver extends SoapFaultMappingExceptionResolver {

    private static final String NAMESPACE = "http://parameta.com/employees/v1";

    public SoapExceptionResolver() {
        SoapFaultDefinition defaultFault = new SoapFaultDefinition();
        defaultFault.setFaultCode(SoapFaultDefinition.SERVER);
        defaultFault.setFaultStringOrReason("Internal service error");
        setDefaultFault(defaultFault);

        Properties mappings = new Properties();
        mappings.setProperty(EmployeeAlreadyExistsException.class.getName(),
                "CLIENT,Employee is already registered");
        mappings.setProperty(InvalidEmployeeDataException.class.getName(),
                "CLIENT,Invalid employee data");
        setExceptionMappings(mappings);

        setOrder(1);
    }

    @Override
    protected void customizeFault(Object endpoint, Exception ex, SoapFault fault) {
        SoapFaultDetail detail = fault.addFaultDetail();
        detail.addFaultDetailElement(new QName(NAMESPACE, "errorCode"))
                .addText(errorCodeFor(ex));
    }

    private String errorCodeFor(Exception ex) {
        if (ex instanceof EmployeeAlreadyExistsException) {
            return "EMPLOYEE_ALREADY_EXISTS";
        }
        if (ex instanceof InvalidEmployeeDataException) {
            return "INVALID_EMPLOYEE_DATA";
        }
        return "INTERNAL_ERROR";
    }
}