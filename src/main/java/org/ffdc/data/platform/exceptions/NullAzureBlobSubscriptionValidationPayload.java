package org.ffdc.data.platform.exceptions;

public class NullAzureBlobSubscriptionValidationPayload extends Exception {
    public NullAzureBlobSubscriptionValidationPayload(String errorMessage) {
        super(errorMessage);
    }    
}
