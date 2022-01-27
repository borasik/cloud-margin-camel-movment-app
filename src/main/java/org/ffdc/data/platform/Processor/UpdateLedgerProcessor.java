package org.ffdc.data.platform.Processor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.ffdc.data.platform.Models.AzureCosmosLedgerPojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.ThroughputProperties;

@Component
public class UpdateLedgerProcessor implements Processor {

    private static final Logger Log = LoggerFactory.getLogger(UpdateLedgerProcessor.class);

    @Value("${cosmos.data-base-name}")
    protected String dataBaseName;

    @Value("${cosmos.container-name}")
    protected String cosmosContainerName;

	@Value("${cosmos.key}")
	protected String cosmosKey= "";

    @Value("${cosmos.serviceEndpoint}")
	protected String serviceEndpoint = "";

    private CosmosDatabase database;
    private CosmosContainer container;
    CosmosClient client;

    @Override
    public void process(Exchange exchange) throws Exception {

        client = new CosmosClientBuilder()
        .endpoint(serviceEndpoint)
        .key(cosmosKey)
        .consistencyLevel(ConsistencyLevel.EVENTUAL)
        .contentResponseOnWriteEnabled(true)
        .buildClient();

        createDatabaseIfNotExists();
        createContainerIfNotExists();

        String storageName = exchange.getProperty("storageName", String.class);
        String dataSetId = exchange.getProperty("dataSetId", String.class);
        String fileName = exchange.getProperty("fileName", String.class);
        String fileExtension = exchange.getProperty("fileExtension", String.class);
        String ledgerStatus = exchange.getProperty("ledgerStatus", String.class);
        String tenantName = exchange.getProperty("tenantName", String.class);

        // TODO: Check if not null
        String processingUniqueId = exchange.getProperty("processingUniqueId", String.class);

        String documentId = UUID.randomUUID().toString();
        AzureCosmosLedgerPojo azureCosmosLedgerPojo = new AzureCosmosLedgerPojo();
        azureCosmosLedgerPojo.setId(documentId);
        azureCosmosLedgerPojo.setDataSetId(dataSetId);
        azureCosmosLedgerPojo.setStorage(storageName);
        azureCosmosLedgerPojo.setTenant(tenantName);
        azureCosmosLedgerPojo.setFile(fileName + "." + fileExtension);
        azureCosmosLedgerPojo.setStatus(ledgerStatus);
        azureCosmosLedgerPojo.setUpdatedAt(new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        azureCosmosLedgerPojo.setPartitionKey(tenantName);
        azureCosmosLedgerPojo.setProcessingUniqueId(processingUniqueId);

        container.createItem(azureCosmosLedgerPojo, new PartitionKey(azureCosmosLedgerPojo.getPartitionKey()), new CosmosItemRequestOptions());

        client.close();
    }

    private void createDatabaseIfNotExists() throws Exception {
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(dataBaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());
    }

    private void createContainerIfNotExists() throws Exception {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties("ledger", "/_partitionKey");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);
        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties, throughputProperties);
        container = database.getContainer(containerResponse.getProperties().getId());
    }
}
