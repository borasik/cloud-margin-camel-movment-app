package org.ffdc.data.platform.Processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.ffdc.data.platform.Exceptions.ArgumentEmptyOrBlankException;
import org.ffdc.data.platform.Exceptions.WrongAzureBlobMessageTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AcceptedRequestTypeNavigatorProcessor implements Processor {
    private static final String IS_AZURE_DATA_LAKE_VALIDATION_SUBSCRIPTION_MESSAGE = "IsAzureDataLakeValidationSubscriptionMessage";

    private static final Logger Log = LoggerFactory.getLogger(AcceptedRequestTypeNavigatorProcessor.class);

    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);

        String eventSubscriptionValidationHeader = exchange.getIn().getHeader("aeg-event-type", String.class);

        if(eventSubscriptionValidationHeader == null) {
            throw new ArgumentEmptyOrBlankException("Event Subscription Validation Header Null. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
        }

        Log.info("Event Subscription Validation Header Received: {}", eventSubscriptionValidationHeader);

        Log.info("Request From Azure Data Lake Received: {}", body);
        if (eventSubscriptionValidationHeader.equals("Notification"))
        {
            exchange.setProperty(IS_AZURE_DATA_LAKE_VALIDATION_SUBSCRIPTION_MESSAGE, "false");
            Log.info("Blob Change Event From Azure Data Lake Received: {}", body);
        }
        else if (eventSubscriptionValidationHeader.equals("SubscriptionValidation"))
        {
            exchange.setProperty(IS_AZURE_DATA_LAKE_VALIDATION_SUBSCRIPTION_MESSAGE, "true");
            Log.info("Blob Event Subscription Validation Request From Azure Data Lake Received: {}", body);
        }
        else if (eventSubscriptionValidationHeader.equals("SubscriptionDeletion"))
        {
            Log.info("Blob Event 'SubscriptionDeletion' From Azure Data Lake Received: {}...Ignoring...", body);
        }
        else {
                throw new WrongAzureBlobMessageTypeException("Request Type Received not Recognized (nor blob Event Request nor Validation Request). Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
            }
        }
}
