package org.ffdc.data.platform.exceptions;

public class WrongAzureBlobMessageType extends Exception {
    public WrongAzureBlobMessageType(String errorMessage) {
        super(errorMessage);
    }
}
