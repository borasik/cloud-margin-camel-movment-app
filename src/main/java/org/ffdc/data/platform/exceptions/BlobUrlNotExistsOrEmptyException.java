package org.ffdc.data.platform.exceptions;

public class BlobUrlNotExistsOrEmptyException extends NullAzureBlobCreateBlobEventPayloadException {
    public BlobUrlNotExistsOrEmptyException(String errorMessage) {
        super(errorMessage);
    }
}
