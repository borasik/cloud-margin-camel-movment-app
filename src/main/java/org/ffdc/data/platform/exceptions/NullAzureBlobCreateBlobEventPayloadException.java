package org.ffdc.data.platform.Exceptions;

public class NullAzureBlobCreateBlobEventPayloadException extends Exception {
    public NullAzureBlobCreateBlobEventPayloadException(String errorMessage) {
        super(errorMessage);
    }
}