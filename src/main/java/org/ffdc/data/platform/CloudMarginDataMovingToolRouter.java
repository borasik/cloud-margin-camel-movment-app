package org.ffdc.data.platform;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

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

import jdk.internal.org.jline.utils.Log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.ffdc.data.platform.Exceptions.ArgumentEmptyOrBlankException;
import org.ffdc.data.platform.Exceptions.BlobUrlNotExistsOrEmptyException;
import org.ffdc.data.platform.Exceptions.NullAzureBlobCreateBlobEventPayloadException;
import org.ffdc.data.platform.Exceptions.NullAzureBlobSubscriptionValidationPayloadException;
import org.ffdc.data.platform.Exceptions.WrongAzureBlobMessageTypeException;
import org.ffdc.data.platform.Exceptions.WrongAzureDataLakeEventTypeException;
import org.ffdc.data.platform.Helpers.UrlParser;
import org.ffdc.data.platform.Models.AzureBlobSubscriptionValidationResponseBody;
import org.ffdc.data.platform.Models.AzureBlobCreateBlobEventPayload.AzureBlobCreateBlobEventPayload;
import org.ffdc.data.platform.Models.AzureBlobSubscriptionValidationPayload.AzureBlobSubscriptionValidationPayload;
import org.ffdc.data.platform.Processor.RedeliveryProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CloudMarginDataMovingToolRouter extends RouteBuilder {

    @Autowired
    private UrlParser urlParser;

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
                    .addParameter("useUserKnownHostsFile", "false")            
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

                    Map<String, Object> headers = exchange.getIn().getHeaders();

                    CloudMarginDataMovingToolRouterLog.info("Headers: " + headers.toString());

                    if(eventSubscriptionValidationHeader == null) {
                        throw new ArgumentEmptyOrBlankException("Event Subscription Validation Header Null. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
                    }

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
                            throw new WrongAzureBlobMessageTypeException("Request Type Received not Recognized (nor blob Event Request nor Validation Request). Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
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
                    throw new NullAzureBlobCreateBlobEventPayloadException("AzureBlobCreateBlobEventPayload[] is Null. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
                }               

                if(azureBlobCreateBlobEventPayload[0] == null || azureBlobCreateBlobEventPayload[0].getData() == null || 
                   azureBlobCreateBlobEventPayload[0].getData().getUrl().isEmpty() || azureBlobCreateBlobEventPayload[0].getData().getUrl().isBlank())
                {
                       throw new BlobUrlNotExistsOrEmptyException("Blob Url Received in Event Message is Empty or not Exists. Location: Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
                }
                
                if(!azureBlobCreateBlobEventPayload[0].getEventType().equals("Microsoft.Storage.BlobCreated"))
                {
                    throw new WrongAzureDataLakeEventTypeException("Data Lake Event Type Received not Equal to Microsoft.Storage.BlobCreated");
                }

                URI blobUrl = new URI(azureBlobCreateBlobEventPayload[0].getData().getUrl());
                String storageName = urlParser.getStorageName(blobUrl);
                String containerTenantName = urlParser.getContainerTenantName(blobUrl);
                String dataSetId = urlParser.getDataSetId(blobUrl);
                String fileName = urlParser.getFileName(blobUrl);
                String fileExtension = urlParser.getFileExtension(blobUrl);

                if(containerTenantName.isEmpty() || containerTenantName.isBlank()) {
                    throw new ArgumentEmptyOrBlankException("Storage Name is Blank or Empty. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter ");
                }

                if(storageName.isEmpty() || storageName.isBlank()) {
                    throw new ArgumentEmptyOrBlankException("container Tenant Name is Blank or Empty. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter ");
                }

                if(dataSetId.isEmpty() || dataSetId.isBlank()) {
                    throw new ArgumentEmptyOrBlankException("Data Set Id is Blank or Empty. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter ");
                }

                if(fileName.isEmpty() || fileName.isBlank()) {
                    throw new ArgumentEmptyOrBlankException("File Name is Blank or Empty. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter ");
                }               
                
                CloudMarginDataMovingToolRouterLog.info(String.format("storageName: %s", storageName));
                CloudMarginDataMovingToolRouterLog.info(String.format("containerTenantName: %s", containerTenantName));
                CloudMarginDataMovingToolRouterLog.info(String.format("dataSetId: %s", dataSetId));
                CloudMarginDataMovingToolRouterLog.info(String.format("fileName: %s", fileName));
                CloudMarginDataMovingToolRouterLog.info(String.format("fileExtension: %s", fileExtension));

                String commandToPullDataFromAzureDataLake = "azure-storage-datalake:" + storageName + 
                                                            "/" + containerTenantName +                                                             
                                                            "?operation=getFile" + 
                                                            "&fileName=" + 
                                                             dataSetId + "/" + fileName + "." + fileExtension + 
                                                             "&dataLakeServiceClient=#dataLakeFileSystemClient&bridgeErrorHandler=false";
                exchange.setProperty("commandToPullDataFromAzureDataLake", commandToPullDataFromAzureDataLake);
                CloudMarginDataMovingToolRouterLog.info(String.format("commandToPullDataFromAzureDataLake: %s", commandToPullDataFromAzureDataLake);

                String fullFileNameToStore = fileName + "_" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + "." + fileExtension;
                exchange.setProperty("CamelFileName", fullFileNameToStore);
                
            })
            .log("Pulling Data Set From: ${exchangeProperty.commandToPullDataFromAzureDataLake}")         
            .to("log:?level=INFO&showBody=true&logMask=true")                 
            .toD("${exchangeProperty.commandToPullDataFromAzureDataLake}")            
            .log("Data Set has been Pulled Successfully from: ${exchangeProperty.commandToPullDataFromAzureDataLake}")
            .to("log:?level=INFO&showBody=true&logMask=true")
            .log(String.format("Pushing Data Set to %s://%s:%s/%s", sftpSchema, sftpHost, sftpPort, sftpPath))
            .to("log:?level=INFO&showBody=true&logMask=true")
            .log("Setting Up Header for CamelFileName to Upload to SFTP: " + "${exchangeProperty.CamelFileName}")
            .setHeader("CamelFileName", simple("${exchangeProperty.CamelFileName}"))
            .to(fromFtpUrl.toString())
            .setHeader(Exchange.HTTP_RESPONSE_CODE, simple("200"))      
            .endRest();

        from("direct:blob-azure-subscription-handshake-response")
            .log("Starting Subscription Validation Process...")
            .unmarshal()
            .json(JsonLibrary.Jackson, AzureBlobSubscriptionValidationPayload[].class)
            .process(exchange -> {
                AzureBlobSubscriptionValidationPayload[] azureBlobSubscriptionValidationPayload = exchange.getIn().getBody(AzureBlobSubscriptionValidationPayload[].class);

                if(azureBlobSubscriptionValidationPayload == null)
                {
                    throw new NullAzureBlobSubscriptionValidationPayloadException("AzureBlobSubscriptionValidationPayload[] is Null. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
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
