package org.ffdc.data.platform.Processor;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ThroughputProperties;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.ffdc.data.platform.Exceptions.ArgumentEmptyOrBlankException;
import org.ffdc.data.platform.Exceptions.AzureCosmosResponseIsEmptyOrNull;
import org.ffdc.data.platform.Exceptions.BlobUrlNotExistsOrEmptyException;
import org.ffdc.data.platform.Exceptions.NullAzureBlobCreateBlobEventPayloadException;
import org.ffdc.data.platform.Exceptions.WrongAzureDataLakeEventTypeException;
import org.ffdc.data.platform.Models.TenantPojo;
import org.ffdc.data.platform.Models.AzureBlobCreateBlobEventPayload.AzureBlobCreateBlobEventPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ffdc.data.platform.Helpers.UrlParser;

@Component
public class GetDataSetFromAzureStorageProcessor implements Processor {

    private static final Logger Log = LoggerFactory.getLogger(GetDataSetFromAzureStorageProcessor.class);

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
    private CosmosClient client;

    @Autowired
    private UrlParser urlParser;

    @Autowired
    private ObjectMapper mapper;

	@Override
	public void process(Exchange exchange) throws Exception {

        String azureBlobCreateBlobEventPayloadString = exchange.getIn().getBody(String.class);
        AzureBlobCreateBlobEventPayload[] azureBlobCreateBlobEventPayload = mapper.readValue(azureBlobCreateBlobEventPayloadString, AzureBlobCreateBlobEventPayload[].class);                   

        if(azureBlobCreateBlobEventPayload == null)
        {
            throw new NullAzureBlobCreateBlobEventPayloadException("AzureBlobCreateBlobEventPayload[] is Null. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
        }

        if(azureBlobCreateBlobEventPayload[0] == null || azureBlobCreateBlobEventPayload[0].getData() == null ||
           azureBlobCreateBlobEventPayload[0].getData().getUrl().isEmpty() || azureBlobCreateBlobEventPayload[0].getData().getUrl().isBlank())
        {
               throw new BlobUrlNotExistsOrEmptyException("Blob Url Received in Event Message is Empty or not Exists. Location: Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter");
        }

        if(!azureBlobCreateBlobEventPayload[0].getEventType().equals("Microsoft.Storage.BlobCreated"))
        {
            throw new WrongAzureDataLakeEventTypeException("Data Lake Event Type Received not Equal to Microsoft.Storage.BlobCreated");
        }

        URI blobUrl = new URI(azureBlobCreateBlobEventPayload[0].getData().getUrl());

        String storageName = urlParser.getStorageName(blobUrl);
        String containerName = urlParser.getContainerName(blobUrl);
        String dataSetId = urlParser.getDataSetId(blobUrl);
        String fileName = urlParser.getFileName(blobUrl);
        String fileExtension = urlParser.getFileExtension(blobUrl);
        String tenantName = urlParser.getTenantName(blobUrl);
        String fullFileName = urlParser.getFullFileName(blobUrl);

        exchange.setProperty("storageName", storageName);
        exchange.setProperty("containerName", containerName);
        exchange.setProperty("dataSetId", dataSetId);
        exchange.setProperty("fileName", fileName);
        exchange.setProperty("fileExtension", fileExtension);
        exchange.setProperty("tenantName", tenantName);
        exchange.setProperty("fullFileName", fullFileName);

        if(containerName.isEmpty() || containerName.isBlank()) {
            throw new ArgumentEmptyOrBlankException("Storage Name is Blank or Empty. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter ");
        }

        if(storageName.isEmpty() || storageName.isBlank()) {
            throw new ArgumentEmptyOrBlankException("container Tenant Name is Blank or Empty. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter ");
        }

        if(dataSetId.isEmpty() || dataSetId.isBlank()) {
            throw new ArgumentEmptyOrBlankException("Data Set Id is Blank or Empty. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter ");
        }

        if(fileName.isEmpty() || fileName.isBlank()) {
            throw new ArgumentEmptyOrBlankException("File Name is Blank or Empty. Location: org.ffdc.data.platform.CloudMarginDataMovingToolRouter ");
        }

        String commandToPullDataFromAzureDataLake = "azure-storage-datalake:" + storageName +
                                                    "/" + containerName +
                                                    "?operation=getFile" +
                                                    "&fileName=" +
                                                    tenantName + "/" + dataSetId + "/" + fullFileName +
                                                     "&dataLakeServiceClient=#dataLakeFileSystemClient&bridgeErrorHandler=false";

        exchange.setProperty("commandToPullDataFromAzureDataLake", commandToPullDataFromAzureDataLake);

        String mcTenantName = mapFfdcTenantToCmTenant(tenantName);

        String fullFileNameToStore = mcTenantName + "_" + dataSetId + "_" + new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()) + "." + fileExtension;
        exchange.setProperty("CamelFileName", fullFileNameToStore);
	}

    private String mapFfdcTenantToCmTenant(String containerTenantName) throws Exception
    {
        Log.info("Start Fetching Tenant Mapping for Tenant: {}", containerTenantName);

        client = new CosmosClientBuilder()
                .endpoint(serviceEndpoint)
                .key(cosmosKey)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .contentResponseOnWriteEnabled(true)
                .buildClient();

        createDatabaseIfNotExists();
        createContainerIfNotExists();

        String sql = String.format("SELECT TOP 1 * FROM tenants c WHERE c.ffdctenantname = '%s'", containerTenantName);

        CosmosPagedIterable<TenantPojo> selectedTenants = container.queryItems(sql, new CosmosQueryRequestOptions(), TenantPojo.class);

        if(selectedTenants == null || !selectedTenants.iterator().hasNext()){
            throw new AzureCosmosResponseIsEmptyOrNull("selectedTenants Result is Null or Empty in GetDataSetFromAzureStorageProcessor.process.mapFfdcTenantToCmTenant()");
        }

        if (selectedTenants.iterator().hasNext()) {
            TenantPojo tenantPojo = selectedTenants.iterator().next();
            if(tenantPojo != null && !tenantPojo.getCmTenantName().isEmpty() && !tenantPojo.getCmTenantName().isBlank())
            {
                Log.info("Tenant Mapping for Tenant: {} Successful to Tenant {}", containerTenantName, tenantPojo.getCmTenantName());
                return tenantPojo.getCmTenantName();
            }
            else
            {
                throw new AzureCosmosResponseIsEmptyOrNull("TenantPojo is Null or Empty in GetDataSetFromAzureStorageProcessor.process.mapFfdcTenantToCmTenant()");
            }
        }

        client.close();
        return "";
    }

    private void createDatabaseIfNotExists() throws Exception {
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(dataBaseName);
        database = client.getDatabase(databaseResponse.getProperties().getId());
    }

    private void createContainerIfNotExists() throws Exception {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties("tenants", "/id");
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);
        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties, throughputProperties);
        container = database.getContainer(containerResponse.getProperties().getId());
    }
}
