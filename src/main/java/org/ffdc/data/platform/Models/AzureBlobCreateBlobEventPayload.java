package org.ffdc.data.platform.Models;

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
@JsonPropertyOrder({ "topic", "subject", "eventType", "id", "data", "dataVersion", "metadataVersion", "eventTime" })
@Generated("jsonschema2pojo")
public class AzureBlobCreateBlobEventPayload {

    @JsonProperty("topic")
    private String topic;
    @JsonProperty("subject")
    private String subject;
    @JsonProperty("eventType")
    private String eventType;
    @JsonProperty("id")
    private String id;
    @JsonProperty("data")
    private Data data;
    @JsonProperty("dataVersion")
    private String dataVersion;
    @JsonProperty("metadataVersion")
    private String metadataVersion;
    @JsonProperty("eventTime")
    private String eventTime;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("topic")
    public String getTopic() {
        return topic;
    }

    @JsonProperty("topic")
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @JsonProperty("subject")
    public String getSubject() {
        return subject;
    }

    @JsonProperty("subject")
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    @JsonProperty("eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("data")
    public Data getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(Data data) {
        this.data = data;
    }

    @JsonProperty("dataVersion")
    public String getDataVersion() {
        return dataVersion;
    }

    @JsonProperty("dataVersion")
    public void setDataVersion(String dataVersion) {
        this.dataVersion = dataVersion;
    }

    @JsonProperty("metadataVersion")
    public String getMetadataVersion() {
        return metadataVersion;
    }

    @JsonProperty("metadataVersion")
    public void setMetadataVersion(String metadataVersion) {
        this.metadataVersion = metadataVersion;
    }

    @JsonProperty("eventTime")
    public String getEventTime() {
        return eventTime;
    }

    @JsonProperty("eventTime")
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
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