package org.ffdc.data.platform;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.commons.httpclient.util.URIUtil;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.ffdc.data.platform.Models.AzureBlobCreateBlobEventPayload.AzureBlobCreateBlobEventPayload;
import org.ffdc.data.platform.Models.AzureBlobSubscriptionValidationPayload.AzureBlobSubscriptionValidationPayload;
import org.ffdc.data.platform.Processor.RedeliveryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CloudMarginDataMovingToolRouter extends RouteBuilder {

    @Value("${app.general.max.redelivery.attempts:2}")
	protected int maxRedeliveryAttempts = 0;
	@Value("${app.general.redelivery.delay:1000}")
	protected int redeliveryInterval = 0;
    private static final Logger Log = LoggerFactory.getLogger(CloudMarginDataMovingToolRouter.class);
    private static final String Is_Azure_DataLake_Validation_Subscription_Message = "isAzureDataLakeValidationSubscriptionMessage";

    @Autowired
	protected RedeliveryProcessor redeliveryProcessor;

    @Override
    public void configure() {
        restConfiguration().component("servlet").bindingMode(RestBindingMode.json);         

        //Predicate validationMessage = PredicateBuilder.and(body().contains("validationCode"), body().contains("validationUrl"));

        onException(Exception.class)
		.maximumRedeliveries(maxRedeliveryAttempts).redeliveryDelay(redeliveryInterval * 1000L)
		.onRedelivery(redeliveryProcessor)
		.logStackTrace(true)
		.log(LoggingLevel.ERROR, "Exception detected. Quitting...")
		.handled(true)
		.wireTap("bean:exitHandler?method=errorReport")
		.stop();

        // Call API localhost:8080/api/cloud-margin/v1/camel/pull-data
        rest("pull-data")
        .post()
        .consumes("application/json")
        .route()
        //.transform().simple("${body}")
        .log("BODY: .......... ${body}")
        .log("Headers: .......... ${headers}")
        .marshal().json(JsonLibrary.Gson, String.class)                
        .process(new Processor() {
            public void process(Exchange exchange) throws Exception {
                String body = exchange.getIn().getBody(String.class);
                Log.info("BODY22:" + body);
                if(body.contains("blobUrl")){
                    exchange.setProperty(Is_Azure_DataLake_Validation_Subscription_Message, "false");                    
                }
                else if(body.contains("validationCode")){
                    exchange.setProperty(Is_Azure_DataLake_Validation_Subscription_Message, "true");
                    Log.info(body);
                    Log.info("Azure Data Lake Event Subscription Validation Message Received");
                }
                else {
                   throw new Exception("Message Type Received not Recognized (Nor blob message nor validation message). In CloudMarginDataMovingToolRouter");
                }
            }
        })
        .choice()
        .when(exchangeProperty(Is_Azure_DataLake_Validation_Subscription_Message).isEqualTo("false")).to("direct:get-data-set-from-azure-dl")
        .when(exchangeProperty(Is_Azure_DataLake_Validation_Subscription_Message).isEqualTo("true")).to("direct:blob-azure-subscription-handshake-response");



        from("direct:get-data-set-from-azure-dl")
        .log("Start Pulling Data Set From ADSL...")
        .routeId("get-data-set-from-azure-dl")
        .unmarshal().json(JsonLibrary.Jackson, AzureBlobCreateBlobEventPayload[].class)
        .process(exchange -> {
            AzureBlobCreateBlobEventPayload[] azureBlobCreateBlobEventPayload = exchange.getIn().getBody(AzureBlobCreateBlobEventPayload[].class);
            // TODO: Try Catch
            exchange.setProperty("blobUrl", azureBlobCreateBlobEventPayload[0].getData().getUrl());
        })
        .log("peorperty: ${exchangeProperty.blobUrl}")
        .to("azure-storage-datalake:p01d15201500001/cloud-margin?operation=getFile&fileName=test.csv&dataLakeServiceClient=#dataLakeFileSystemClient&bridgeErrorHandler=false")
        .to("file://C://Users//ab5645//Downloads/fff.xlsx");


        from("direct:blob-azure-subscription-handshake-response")
        .log("Starting Subscription Validation Process...")
        .unmarshal().json(JsonLibrary.Jackson, AzureBlobSubscriptionValidationPayload[].class)
        .process(exchange -> {
            AzureBlobSubscriptionValidationPayload[] azureBlobSubscriptionValidationPayload = exchange.getIn().getBody(AzureBlobSubscriptionValidationPayload[].class);
            // TODO: Try Catch
            URI uri = new URI(azureBlobSubscriptionValidationPayload[0].getData().getValidationUrl()); 
            log.info("uri alex: " + uri);      
            log.info("uri query alex: " + uri.getQuery()); 
            log.info("uri path alex: " + uri.getPath());          
            log.info("uri host alex: " + uri.getHost());          
            log.info("uri port alex: " + uri.getPort());
            
            exchange.setProperty("query", URIUtil.encodeQuery(uri.getQuery()));
            // exchange.getIn().setHeader(Exchange.HTTP_QUERY, uri.getQuery());                  
            // exchange.getIn().setHeader(Exchange.HTTP_METHOD, "GET");  
            String url = URIUtil.encodeQuery("https://" + uri.getHost() + ":" + uri.getPort() + uri.getPath()); 
            exchange.setProperty("validationUrlPath", url);            
        })
        .routeId("blob-azure-subscription-handshake-response")
        .log("I found validationUrl :${exchangeProperty.validationUrlPath}")        
        .log("I found validationUrlQuery :${exchangeProperty.query}")  
        .setHeader(Exchange.HTTP_QUERY, simple("${exchangeProperty.query}"))
        .setHeader(Exchange.HTTP_METHOD, simple("GET"))
        .setHeader(Exchange.HTTP_METHOD, simple("GET"));        
        //.toD("${exchangeProperty.validationUrlPath&bridgeEndpoint=true&userAgent=PostmanRuntime/7.28.4}");
        //.to("https://rp-canadacentral.eventgrid.azure.net:553/eventsubscriptions/rrr/validate?id=71BBB039-B863-45FE-B1C5-3DA27D99DD8A&t=2021-11-26T21:56:56.4450271Z&apiVersion=2020-10-15-preview&token=LprRsbqGe8mMUdH41FOqNzzq3w57TZtwZEgyVQUTJN0=&bridgeEndpoint=true");
    }

}

