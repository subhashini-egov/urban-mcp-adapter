package org.egov.lg.mcp.ccrs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
public class UrbanMCPConfiguration {
    
    // Base URLs
    @Value("${urban.mcp.demo.base-url}")
    private String demoBaseUrl;
    
    @Value("${urban.mcp.local.base-url}")
    private String localBaseUrl;
    
    // API Endpoints
    @Value("${urban.mcp.endpoints.pgr.search}")
    private String pgrSearchEndpoint;
    
    @Value("${urban.mcp.endpoints.pgr.create}")
    private String pgrCreateEndpoint;
    
    @Value("${urban.mcp.endpoints.mdms.search}")
    private String mdmsSearchEndpoint;
    
    @Value("${urban.mcp.endpoints.location.search}")
    private String locationSearchEndpoint;
    
    // Date and Time Configuration
    @Value("${urban.mcp.date.format}")
    private String dateFormat;
    
    @Value("${urban.mcp.timezone}")
    private String timezone;
    
    // Service Configuration
    @Value("${urban.mcp.service.source}")
    private String serviceSource;
    
    @Value("${urban.mcp.service.workflow.action}")
    private String workflowAction;
    
    // Location Configuration
    @Value("${urban.mcp.location.hierarchy-type}")
    private String hierarchyType;
    
    @Value("${urban.mcp.location.boundary-type}")
    private String boundaryType;
    
    // MDMS Configuration
    @Value("${urban.mcp.mdms.module.rainmaker-pgr}")
    private String moduleRainmakerPgr;
    
    @Value("${urban.mcp.mdms.module.tenant}")
    private String moduleTenant;
    
    @Value("${urban.mcp.mdms.master.service-defs}")
    private String masterServiceDefs;
    
    @Value("${urban.mcp.mdms.master.tenants}")
    private String masterTenants;
    
    @Value("${urban.mcp.mdms.master.city-module}")
    private String masterCityModule;
    
    // Authentication Configuration
    @Value("${urban.mcp.auth.username}")
    private String authUsername;
    
    @Value("${urban.mcp.auth.password}")
    private String authPassword;
    
    @Value("${urban.mcp.auth.tenant-id}")
    private String authTenantId;
    
    @Value("${urban.mcp.auth.user-type}")
    private String authUserType;
    
    @Value("${urban.mcp.auth.scope}")
    private String authScope;
    
    @Value("${urban.mcp.auth.grant-type}")
    private String authGrantType;
    
    @Value("${urban.mcp.auth.client-id}")
    private String authClientId;
    
    @Value("${urban.mcp.auth.client-secret}")
    private String authClientSecret;
}
