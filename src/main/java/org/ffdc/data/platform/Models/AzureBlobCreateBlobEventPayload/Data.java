package org.ffdc.data.platform.Models.AzureBlobCreateBlobEventPayload;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "api", "clientRequestId", "requestId", "eTag", "contentType", "contentLength", "blobType",
        "blobUrl", "url", "sequencer", "identity", "storageDiagnostics" })
@Generated("jsonschema2pojo")
public class Data {

    @JsonProperty("api")
    private String api;
    @JsonProperty("clientRequestId")
    private String clientRequestId;
    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("eTag")
    private String eTag;
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("contentLength")
    private Integer contentLength;
    @JsonProperty("blobType")
    private String blobType;
    @JsonProperty("blobUrl")
    private String blobUrl;
    @JsonProperty("url")
    private String url;
    @JsonProperty("sequencer")
    private String sequencer;
    @JsonProperty("identity")
    private String identity;
    @JsonProperty("storageDiagnostics")
    private StorageDiagnostics storageDiagnostics;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("api")
    public String getApi() {
        return api;
    }

    @JsonProperty("api")
    public void setApi(String api) {
        this.api = api;
    }

    @JsonProperty("clientRequestId")
    public String getClientRequestId() {
        return clientRequestId;
    }

    @JsonProperty("clientRequestId")
    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    @JsonProperty("requestId")
    public String getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("eTag")
    public String geteTag() {
        return eTag;
    }

    @JsonProperty("eTag")
    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    @JsonProperty("contentType")
    public String getContentType() {
        return contentType;
    }

    @JsonProperty("contentType")
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @JsonProperty("contentLength")
    public Integer getContentLength() {
        return contentLength;
    }

    @JsonProperty("contentLength")
    public void setContentLength(Integer contentLength) {
        this.contentLength = contentLength;
    }

    @JsonProperty("blobType")
    public String getBlobType() {
        return blobType;
    }

    @JsonProperty("blobType")
    public void setBlobType(String blobType) {
        this.blobType = blobType;
    }

    @JsonProperty("blobUrl")
    public String getBlobUrl() {
        return blobUrl;
    }

    @JsonProperty("blobUrl")
    public void setBlobUrl(String blobUrl) {
        this.blobUrl = blobUrl;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("sequencer")
    public String getSequencer() {
        return sequencer;
    }

    @JsonProperty("sequencer")
    public void setSequencer(String sequencer) {
        this.sequencer = sequencer;
    }

    @JsonProperty("identity")
    public String getIdentity() {
        return identity;
    }

    @JsonProperty("identity")
    public void setIdentity(String identity) {
        this.identity = identity;
    }

    @JsonProperty("storageDiagnostics")
    public StorageDiagnostics getStorageDiagnostics() {
        return storageDiagnostics;
    }

    @JsonProperty("storageDiagnostics")
    public void setStorageDiagnostics(StorageDiagnostics storageDiagnostics) {
        this.storageDiagnostics = storageDiagnostics;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
