package org.ffdc.data.platform;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.ffdc.data.platform.Models.AzureBlobCreateBlobEventPayload;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.ffdc.data.platform.Processor.RedeliveryProcessor;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints
 * to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class CloudMaringDataMovingToolRouter extends RouteBuilder {

    @Value("${app.general.max.redelivery.attempts:2}")
	protected int maxRedeliveryAttemps = 0;
	@Value("${app.general.redelivery.delay:1000}")
	protected int redeliveryInterval = 0;
    
    @Autowired
	protected RedeliveryProcessor redeliveryProcessor;

    @Override
    public void configure() {
        restConfiguration().component("servlet").bindingMode(RestBindingMode.json);       

         JacksonDataFormat jacksonDataFormat = new JacksonDataFormat();
         jacksonDataFormat.setUnmarshalType(AzureBlobCreateBlobEventPayload.class);
         // jacksonDataFormat.useList();
         // ObjectMapper jacksonObjectMapper = new ObjectMapper();
         // jacksonDataFormat.setObjectMapper(jacksonObjectMapper);


        //  onException(Exception.class)
		// .maximumRedeliveries(maxRedeliveryAttemps).redeliveryDelay(redeliveryInterval * 1000L) //makes mill seconds
		// .onRedelivery(redeliveryProcessor)
		// .logStackTrace(true)
		// .log(LoggingLevel.ERROR, "Exception detected. Quitting...")
		// .handled(true)
		// .wireTap("bean:exitHandler?method=errorReport")
		// .stop();

        // Call API localhost:8080/api/cloud-margin/v1/camel/pull-data
        rest("pull-data")
        .post()
        .consumes("application/json")
        .route()
        .transform().simple("${body}")
        .log("BODY: .......... ${body}")
        .marshal().json(JsonLibrary.Jackson, AzureBlobCreateBlobEventPayload.class)
        .log("BODY marshaled: .......... ${body}")
        .process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                String stringBody = exchange.getIn().getBody(String.class);
                String stringBody2 = stringBody.substring( 1, stringBody.length() - 1);
                exchange.getOut().setBody(stringBody2);                
            }})
        .log("${body}")       
        .to("direct:getdata");

        from("direct:getdata").log("getting data from azure data lake...${body}")
        .unmarshal().json(JsonLibrary.Jackson, AzureBlobCreateBlobEventPayload.class)
        .process(exchange -> {
            AzureBlobCreateBlobEventPayload azureBlobCreateBlobEventPayload = exchange.getIn().getBody(AzureBlobCreateBlobEventPayload.class);
            // TODO: Try Catch
            exchange.setProperty("blobUrl", azureBlobCreateBlobEventPayload.getData().getUrl());
        })
        .log("peorperty: ${exchangeProperty.blobUrl}")
        .to("azure-storage-datalake:p01d15201500001/cloud-margin?operation=getFile&fileName=test.csv&dataLakeServiceClient=#dataLakeFileSystemClient&bridgeErrorHandler=false")
        .to("file://C://Users//ab5645//Downloads/fff.xlsx");
    }

}
