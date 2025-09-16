package org.egov.lg.mcp.ccrs.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintSummary {
    @JsonProperty("active")
    private boolean active = true;

    @JsonProperty("id")
    private String id = null;

    @JsonProperty("serviceCode")
    private String serviceCode = null;

    @JsonProperty("tenantId")
    private String tenantId = null;

    @JsonProperty("serviceRequestId")
    private String serviceRequestId = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("applicationStatus")
    private String applicationStatus = null;

    public ComplaintSummary(String serviceRequestId, String applicationStatus, String description, String tenantId) {
        this.serviceRequestId = serviceRequestId;
        this.applicationStatus = applicationStatus;
        this.description = description;
        this.tenantId = tenantId;
    }
}
