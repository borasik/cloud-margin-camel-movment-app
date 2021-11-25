package org.ffdc.data.platform;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.ffdc.data.platform.Models.AzureBlobCreateBlobEventPayload;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
        .transform().simple("${body}")
        .log("BODY: .......... ${body}")
        .log("Headers: .......... ${headers}")
        //.marshal().json(JsonLibrary.Jackson, String.class)
        .log("BODY2: .......... ${body}")
        .choice()
            //.when(body().contains("validationCode"))
            .when(bodyAs(String.class).contains("validationCode"))
                .log("XXXXXXXXXXXXX")
                .to("direct:blob-azure-subscription-handshake-response")
            .when().simple("${body} contains 'blobUrl'")
                .log("YYYYYYYYYYY")
                .to("direct:get-data-set-from-azure-dl");

        from("direct:get-data-set-from-azure-dl")
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
        .routeId("blob-azure-subscription-handshake-response")
        .log("I found validation");
        //.toD();
        
    }

}
