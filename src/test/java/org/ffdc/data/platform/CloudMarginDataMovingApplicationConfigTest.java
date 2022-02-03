package org.ffdc.data.platform;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class CloudMarginDataMovingApplicationConfigTest {

    @Autowired
    CloudMarginDataMovingApplicationConfig cloudMarginDataMovingApplicationConfig;

    @Test
    public void createDataLakeFileSystemClientNotNull(){
        DataLakeServiceClient DataLakeServiceClient = cloudMarginDataMovingApplicationConfig.dataLakeFileSystemClient();

        assertNotNull(DataLakeServiceClient);
    }

    @Test
    public void createCosmosAsyncClientNotNull(){
        CosmosAsyncClient cosmosAsyncClient = cloudMarginDataMovingApplicationConfig.cosmosAsyncClient();

        assertNotNull(cosmosAsyncClient);
    }

    @Test
    public void createObjectMapperNotNull(){
        ObjectMapper objectMapper = cloudMarginDataMovingApplicationConfig.objectMapper();

        assertNotNull(objectMapper);
    }
}
