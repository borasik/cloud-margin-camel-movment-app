package org.ffdc.data.platform.exceptions;

public class NullAzureBlobCreateBlobEventPayload extends Exception {
    public NullAzureBlobCreateBlobEventPayload(String errorMessage) {
        super(errorMessage);
    }
}