package org.ffdc.data.platform.exceptions;

public class WrongAzureDataLakeEventTypeException extends Exception {
    public WrongAzureDataLakeEventTypeException(String errorMessage) {
        super(errorMessage);
    }
}
