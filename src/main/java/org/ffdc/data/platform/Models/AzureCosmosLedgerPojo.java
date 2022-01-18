package org.ffdc.data.platform.Models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "data-set-id", "storage", "tenant", "file", "status", "updated-at", "_partitionKey"})
public class AzureCosmosLedgerPojo {
    
    @JsonProperty("id")
    private String id;

    @JsonProperty("processingUniqueId")    
    private String processingUniqueId;

    @JsonProperty("datasetid")
    private String dataSetId;

    @JsonProperty("storage")
    private String storage;

    @JsonProperty("tenant")
    private String tenant;

    @JsonProperty("file")
    private String file;

    @JsonProperty("status")
    private String status;

    @JsonProperty("updatedat")
    private String updatedAt;

    @JsonProperty("_partitionKey")
    private String partitionKey;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("processingUniqueId")
    public String getProcessingUniqueId() {
        return processingUniqueId;
    }

    @JsonProperty("processingUniqueId")
    public void setProcessingUniqueId(String processingUniqueId) {
        this.processingUniqueId = processingUniqueId;
    }

    @JsonProperty("datasetid")
    public String getDataSetId() {
        return dataSetId;
    }

    @JsonProperty("datasetid")
    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }   

    @JsonProperty("storage")
    public String getStorage() {
        return storage;
    }

    @JsonProperty("storage")
    public void setStorage(String storage) {
        this.storage = storage;
    }

    @JsonProperty("tenant")
    public String getTenant() {
        return tenant;
    }

    @JsonProperty("tenant")
    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    @JsonProperty("file")
    public String getFile() {
        return file;
    }

    @JsonProperty("file")
    public void setFile(String file) {
        this.file = file;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("updatedat")
    public String getUpdatedAt() {
        return updatedAt;
    }

    @JsonProperty("updatedat")
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonProperty("_partitionKey")
    public String getPartitionKey() {
        return partitionKey;
    }

    @JsonProperty("_partitionKey")
    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }
}
