package org.ffdc.data.platform.Exceptions;

public class BlobUrlNotExistsOrEmptyException extends NullAzureBlobCreateBlobEventPayloadException {
    public BlobUrlNotExistsOrEmptyException(String errorMessage) {
        super(errorMessage);
    }
}
