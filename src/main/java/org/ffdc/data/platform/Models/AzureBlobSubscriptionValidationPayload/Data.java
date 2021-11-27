package org.ffdc.data.platform.Models.AzureBlobSubscriptionValidationPayload;

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
@JsonPropertyOrder({ "validationCode", "validationUrl" })
@Generated("jsonschema2pojo")
public class Data {

    @JsonProperty("validationCode")
    private String validationCode;
    @JsonProperty("validationUrl")
    private String validationUrl;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("validationCode")
    public String getValidationCode() {
        return validationCode;
    }

    @JsonProperty("validationCode")
    public void setValidationCode(String validationCode) {
        this.validationCode = validationCode;
    }

    @JsonProperty("validationUrl")
    public String getValidationUrl() {
        return validationUrl;
    }

    @JsonProperty("validationUrl")
    public void setValidationUrl(String validationUrl) {
        this.validationUrl = validationUrl;
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
