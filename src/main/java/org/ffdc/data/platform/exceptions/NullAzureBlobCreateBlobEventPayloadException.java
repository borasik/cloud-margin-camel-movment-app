package org.ffdc.data.platform.exceptions;

public class NullAzureBlobCreateBlobEventPayloadException extends Exception {
    public NullAzureBlobCreateBlobEventPayloadException(String errorMessage) {
        super(errorMessage);
    }
}