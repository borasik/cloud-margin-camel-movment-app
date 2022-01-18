package org.ffdc.data.platform.Models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "id", "ffdctenantname", "cmtenantname" })
public class TenantPojo {

    @JsonProperty("id")
    private String id;

    @JsonProperty("ffdctenantname")
    private String ffdcTenantName;

    @JsonProperty("cmtenantname")
    private String cmTenantName;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("ffdctenantname")
    public String getFfdcTenantName() {
        return ffdcTenantName;
    }

    @JsonProperty("ffdctenantname")
    public void setFfdcTenantName(String ffdcTenantName) {
        this.ffdcTenantName = ffdcTenantName;
    }

    @JsonProperty("cmtenantname")
    public String getCmTenantName() {
        return cmTenantName;
    }

    @JsonProperty("cmtenantname")
    public void setCmTenantName(String cmTenantName) {
        this.cmTenantName = cmTenantName;
    }

}
