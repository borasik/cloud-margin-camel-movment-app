package org.ffdc.data.platform;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.ffdc.data.platform.Processor.AcceptedRequestTypeNavigatorProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AcceptedRequestTypeNavigatorProcessorTest {

    @Autowired
    private AcceptedRequestTypeNavigatorProcessor acceptedRequestTypeNavigatorProcessor;

    @Autowired
    private Exchange exchange;

    @Autowired
    private CamelContext camelContext;

    // @Test
    // public void EventSubscriptionValidationHeaderNotPresentOnRequest(){

    //     exchange.getIn().setBody("hello");
    //     acceptedRequestTypeNavigatorProcessor.process(exchange);

    // }
}
