package org.ffdc.data.platform.exceptions;

public class WrongAzureBlobMessageTypeException extends Exception {
    public WrongAzureBlobMessageTypeException(String errorMessage) {
        super(errorMessage);
    }
}
