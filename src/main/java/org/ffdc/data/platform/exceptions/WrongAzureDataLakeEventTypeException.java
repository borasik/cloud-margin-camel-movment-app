package org.ffdc.data.platform.Exceptions;

public class WrongAzureDataLakeEventTypeException extends Exception {
    public WrongAzureDataLakeEventTypeException(String errorMessage) {
        super(errorMessage);
    }
}
