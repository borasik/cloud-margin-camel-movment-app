package org.ffdc.data.platform.Exceptions;

public class AzureCosmosResponseIsEmptyOrNull extends Exception {
    public AzureCosmosResponseIsEmptyOrNull(String errorMessage) {
        super(errorMessage);
    }
}
