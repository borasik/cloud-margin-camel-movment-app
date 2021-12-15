package org.ffdc.data.platform.Exceptions;

public class WrongAzureBlobMessageTypeException extends Exception {
    public WrongAzureBlobMessageTypeException(String errorMessage) {
        super(errorMessage);
    }
}
