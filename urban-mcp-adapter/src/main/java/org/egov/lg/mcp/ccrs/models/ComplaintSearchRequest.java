package org.egov.lg.mcp.ccrs.models;


import lombok.Data;

@Data
public class ComplaintSearchRequest {
    private String tenantId;
    private String status;
    private String fromDate;  // Epoch time or ISO-8601 string, depending on your API
    private String toDate;
}

