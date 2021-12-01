package org.ffdc.data.platform;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.ffdc.data.platform.Models.AzureBlobSubscriptionValidationResponseBody;
import org.ffdc.data.platform.Models.AzureBlobCreateBlobEventPayload.AzureBlobCreateBlobEventPayload;
import org.ffdc.data.platform.Models.AzureBlobSubscriptionValidationPayload.AzureBlobSubscriptionValidationPayload;
import org.ffdc.data.platform.Processor.RedeliveryProcessor;
import org.ffdc.data.platform.exceptions.NullAzureBlobCreateBlobEventPayload;
import org.ffdc.data.platform.exceptions.NullAzureBlobSubscriptionValidationPayload;
import org.ffdc.data.platform.exceptions.WrongAzureBlobMessageType;
import org.ffdc.data.platform.exceptions.WrongAzureDataLakeEventTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CloudMarginDataMovingToolRouter extends RouteBuilder {

    @Value("${app.general.max.redelivery.attempts:2}")
    protected int maxRedeliveryAttempts = 0;
    @Value("${app.general.redelivery.delay:1000}")
    protected int redeliveryInterval = 0;

    @Value("${app.sftp.schema}")
    protected String sftpSchema;
    @Value("${app.sftp.host}")
    protected String sftpHost;
    @Value("${app.sftp.port}")
    protected int sftpPort = 22;
    @Value("${app.sftp.username}")
    protected String sftpUsername;
    @Value("${app.sftp.secret}")
    protected String sftpSecret;
    @Value("${app.sftp.path}")
    protected String sftpPath;

    private static final Logger CloudMarginDataMovingToolRouterLog = LoggerFactory.getLogger(CloudMarginDataMovingToolRouter.class);
    private static final String Is_Azure_DataLake_Validation_Subscription_Message = "isAzureDataLakeValidationSubscriptionMessage";

    @Autowired
    protected RedeliveryProcessor redeliveryProcessor;

    @Override
    public void configure() throws URISyntaxException {
       
        URI fromFtpUrl = new URIBuilder()
                    .setScheme(sftpSchema)
                    .setHost(sftpHost)
                    .setPort(sftpPort)
                    .setPath(sftpPath)
                    .addParameter("username", sftpUsername)
                    .addParameter("password", sftpSecret)
                    .addParameter("passiveMode", "false")                  
                    .addParameter("readLock", "changed")
                    .addParameter("readLockMinAge", "1m")
                    .addParameter("readLockTimeout", "70000")
                    .addParameter("readLockCheckInterval", "5000")
                    .addParameter("stepwise", "false")                
                    .build();
       


        restConfiguration().component("servlet").bindingMode(RestBindingMode.json);        

        onException(Exception.class).maximumRedeliveries(maxRedeliveryAttempts)
                .redeliveryDelay(redeliveryInterval * 1000L).onRedelivery(redeliveryProcessor).logStackTrace(true)
                .log(LoggingLevel.ERROR, "Exception detected. Quitting...").handled(true)
                .wireTap("bean:exitHandler?method=errorReport").stop();

        // Call API localhost:8080/api/cloud-margin/v1/camel/pull-data
        rest("pull-data")
            .post()            
            .consumes("application/json")          
            .route()            
            .marshal()
            .json(JsonLibrary.Gson, String.class)
            .process(new Processor() {
                public void process(Exchange exchange) throws Exception {
                    String body = exchange.getIn().getBody(String.class);

                    String eventSubscriptionValidationHeader = exchange.getIn().getHeader("aeg-event-type", String.class);
                    CloudMarginDataMovingToolRouterLog.info(String.format("Event Subscription Validation Header Received: %s", eventSubscriptionValidationHeader));

                    CloudMarginDataMovingToolRouterLog.info(String.format("Request From Azure Data Lake Received: %s", body));                    
                    if (eventSubscriptionValidationHeader.equals("Notification")) {
                        exchange.setProperty(Is_Azure_DataLake_Validation_Subscription_Message, "false");
                        CloudMarginDataMovingToolRouterLog.info(String.format("Blob Change Event From Azure Data Lake Received: %s", body));
                       } 
                    else if (eventSubscriptionValidationHeader.equals("SubscriptionValidation")) {
                        exchange.setProperty(Is_Azure_DataLake_Validation_Subscription_Message, "true");                        
                        CloudMarginDataMovingToolRouterLog.info(String.format("Blob Event Subscription Validation Request From Azure Data Lake Received: %s", body));
                        } 
                    else if (eventSubscriptionValidationHeader.equals("SubscriptionDeletion")) 
                    {                        
                        CloudMarginDataMovingToolRouterLog.info(String.format("Blob Event 'SubscriptionDeletion' From Azure Data Lake Received: %s...Ignoring...", body));
                    } 
                    else {
                            throw new WrongAzureBlobMessageType("Request Type Received not Recognized (nor blob Event Request nor Validation Request). Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
                        }
                    }
            }).choice()                
                .when(exchangeProperty(Is_Azure_DataLake_Validation_Subscription_Message)
                    .isEqualTo("false"))
                        .to("direct:get-data-set-from-azure-dl")
                .when(exchangeProperty(Is_Azure_DataLake_Validation_Subscription_Message)
                    .isEqualTo("true"))
                        .to("direct:blob-azure-subscription-handshake-response");

        from("direct:get-data-set-from-azure-dl")
            .log(LoggingLevel.INFO, "Start Pulling Data Set From ADSL...")            
            .routeId("get-data-set-from-azure-dl")
            .unmarshal()
            .json(JsonLibrary.Jackson, AzureBlobCreateBlobEventPayload[].class)
            .process(exchange -> {
                AzureBlobCreateBlobEventPayload[] azureBlobCreateBlobEventPayload = exchange.getIn().getBody(AzureBlobCreateBlobEventPayload[].class);

                if(azureBlobCreateBlobEventPayload == null)
                {
                    throw new NullAzureBlobCreateBlobEventPayload("AzureBlobCreateBlobEventPayload[] is Null. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
                }

                exchange.setProperty("blobUrl", azureBlobCreateBlobEventPayload[0].getData().getUrl());

                if(!azureBlobCreateBlobEventPayload[0].getEventType().equals("Microsoft.Storage.BlobCreated"))
                {
                    throw new WrongAzureDataLakeEventTypeException("Data Lake Event Type Received not Equal to Microsoft.Storage.BlobCreated");
                }
            })
            .log("Pulling Data Set From: ${exchangeProperty.blobUrl}")
            .to("log:?level=INFO&showBody=true&logMask=true")
            // ! TODO: Use blobUrl
            .to("azure-storage-datalake:p01d15201500001/cloud-margin?operation=getFile&fileName=test.csv&dataLakeServiceClient=#dataLakeFileSystemClient&bridgeErrorHandler=false")
            .log("Data Set has been Pulled Successfully from: ${exchangeProperty.blobUrl}")
            .to("log:?level=INFO&showBody=true&logMask=true")
            .log(String.format("Pushing Data Set to %s://%s:%s/%s", sftpSchema, sftpHost, sftpPort, sftpPath))
            .to("log:?level=INFO&showBody=true&logMask=true")
            .to(fromFtpUrl.toString())
            .endRest();

        from("direct:blob-azure-subscription-handshake-response")
            .log("Starting Subscription Validation Process...")
            .unmarshal()
            .json(JsonLibrary.Jackson, AzureBlobSubscriptionValidationPayload[].class)
            .process(exchange -> {
                AzureBlobSubscriptionValidationPayload[] azureBlobSubscriptionValidationPayload = exchange.getIn().getBody(AzureBlobSubscriptionValidationPayload[].class);

                if(azureBlobSubscriptionValidationPayload == null)
                {
                    throw new NullAzureBlobSubscriptionValidationPayload("AzureBlobSubscriptionValidationPayload[] is Null. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
                }
                
                String validationCode = azureBlobSubscriptionValidationPayload[0].getData().getValidationCode();                    
                exchange.setProperty("validationCode", validationCode);                    

                AzureBlobSubscriptionValidationResponseBody azureBlobSubscriptionValidationResponseBody = new AzureBlobSubscriptionValidationResponseBody();
                azureBlobSubscriptionValidationResponseBody.setValidationResponse(validationCode);
                exchange.getIn().setBody(azureBlobSubscriptionValidationResponseBody);

            }).routeId("blob-azure-subscription-handshake-response")
                .log("Sending Validation Code back to Event Grid Subscription ...")
                .to("log:?level=INFO&showBody=true")            
                .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("200"))                
                .endRest();                             
    }
}
