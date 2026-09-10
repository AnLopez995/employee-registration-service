package com.parameta.workforce.api.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;

@Configuration
public class SoapClientConfig {

    @Bean
    public Jaxb2Marshaller employeeMarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath(
                "com.parameta.workforce.api.employee.infrastructure.soap.contract");
        return marshaller;
    }

    @Bean
    public WebServiceTemplate employeeWebServiceTemplate(
            Jaxb2Marshaller employeeMarshaller,
            @Value("${soap.employee-registry.url}") String defaultUri,
            @Value("${soap.employee-registry.connect-timeout}") Duration connectTimeout,
            @Value("${soap.employee-registry.read-timeout}") Duration readTimeout) {

        HttpUrlConnectionMessageSender sender = new HttpUrlConnectionMessageSender();
        sender.setConnectionTimeout(connectTimeout);
        sender.setReadTimeout(readTimeout);

        WebServiceTemplate template = new WebServiceTemplate();
        template.setMarshaller(employeeMarshaller);
        template.setUnmarshaller(employeeMarshaller);
        template.setDefaultUri(defaultUri);
        template.setMessageSender(sender);
        return template;
    }
}