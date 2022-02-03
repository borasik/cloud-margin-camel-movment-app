package org.ffdc.data.platform;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.ffdc.data.platform.Exceptions.ArgumentEmptyOrBlankException;
import org.ffdc.data.platform.Exceptions.NullAzureBlobSubscriptionValidationPayloadException;
import org.ffdc.data.platform.Exceptions.WrongAzureBlobMessageTypeException;
import org.ffdc.data.platform.Models.AzureBlobSubscriptionValidationResponseBody;
import org.ffdc.data.platform.Models.AzureBlobSubscriptionValidationPayload.AzureBlobSubscriptionValidationPayload;
import org.ffdc.data.platform.Processor.AcceptedRequestTypeNavigatorProcessor;
import org.ffdc.data.platform.Processor.GetDataSetFromAzureStorageProcessor;
import org.ffdc.data.platform.Processor.RedeliveryProcessor;
import org.ffdc.data.platform.Processor.UpdateLedgerProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CloudMarginDataMovingToolRouter extends RouteBuilder {

    @Autowired
    private GetDataSetFromAzureStorageProcessor getDataSetFromAzureStorageProcessor;

    @Autowired
    private UpdateLedgerProcessor updateLedgerProcessor;

    @Autowired
    private AcceptedRequestTypeNavigatorProcessor acceptedRequestTypeNavigatorProcessor;

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

    private static final String IS_AZURE_DATA_LAKE_VALIDATION_SUBSCRIPTION_MESSAGE = "IsAzureDataLakeValidationSubscriptionMessage";

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

        rest("pull-data")
            .post()
            .consumes("application/json")
            .route()
            .marshal()
            .json(JsonLibrary.Jackson, String.class)
            .process(acceptedRequestTypeNavigatorProcessor)
            .choice()
                .when(exchangeProperty(IS_AZURE_DATA_LAKE_VALIDATION_SUBSCRIPTION_MESSAGE)
                    .isEqualTo("false"))
                        .to("direct:get-data-set-from-azure-dl")
                .when(exchangeProperty(IS_AZURE_DATA_LAKE_VALIDATION_SUBSCRIPTION_MESSAGE)
                    .isEqualTo("true"))
                        .to("direct:blob-azure-subscription-handshake-response");

        from("direct:get-data-set-from-azure-dl")
            .process(new Processor() {
                public void process(Exchange exchange) throws Exception {
                    String processingUniqueId = UUID.randomUUID().toString();
                    exchange.setProperty("processingUniqueId", processingUniqueId);
                    }
             })
            .log(LoggingLevel.INFO, "Start Pulling Data Set From ADSL...")
            .routeId("get-data-set-from-azure-dl")
            .process(getDataSetFromAzureStorageProcessor)

            .log("BODY: ${body}")
            .setProperty("ledgerStatus", simple("about to pull data set from azure blob storage"))
            .process(updateLedgerProcessor)

            .log("Pulling Data Set From: ${exchangeProperty.commandToPullDataFromAzureDataLake}")
            .toD("${exchangeProperty.commandToPullDataFromAzureDataLake}")

            .setProperty("ledgerStatus", simple("data set has been pulled successfully from azure storage"))
            .process(updateLedgerProcessor)

            .log(String.format("Pushing Data Set to %s://%s:%s/%s", sftpSchema, sftpHost, sftpPort, sftpPath))
            .log("Setting Up Header for CamelFileName to Upload to SFTP: " + "${exchangeProperty.CamelFileName}")
            .setHeader("CamelFileName", simple("${exchangeProperty.CamelFileName}"))
            .to(fromFtpUrl.toString())

            .setProperty("ledgerStatus", simple("data set has been pushed successfully to sftp"))
            .process(updateLedgerProcessor)
            .setBody(simple("HTTP_RESPONSE_CODE 200"))
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
