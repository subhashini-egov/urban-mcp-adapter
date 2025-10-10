package org.egov.lg.mcp.ccrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.egov.lg.mcp.ccrs.models.ComplaintSummary;
import org.egov.lg.mcp.ccrs.models.ServiceResponse;
import org.egov.lg.mcp.ccrs.utils.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service class for Urban MCP (Model Context Protocol) operations.
 * Provides tools for complaint management, tenant operations, and location services.
 */
@Service
public class UrbanMCPService {
    
    private static final Logger log = LoggerFactory.getLogger(UrbanMCPService.class);
    
    // Constants
    private static final String DEMO_BASE_URL = "https://unified-demo.digit.org";
    private static final String LOCAL_BASE_URL = "http://localhost:8090";
    private static final String PGR_SEARCH_ENDPOINT = "/pgr-services/v2/request/_search";
    private static final String PGR_CREATE_ENDPOINT = "/pgr-services/v2/request/_create";
    private static final String MDMS_SEARCH_ENDPOINT = "/egov-mdms-service/v1/_search";
    private static final String LOCATION_SEARCH_ENDPOINT = "/egov-location/location/v11/boundarys/_search";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIMEZONE = "Asia/Kolkata";
    private static final String SOURCE_WEB = "web";
    private static final String WORKFLOW_ACTION_APPLY = "APPLY";
    private static final String HIERARCHY_TYPE_ADMIN = "ADMIN";
    private static final String BOUNDARY_TYPE_LOCALITY = "Locality";
    private static final String MODULE_RAINMAKER_PGR = "RAINMAKER-PGR";
    private static final String MASTER_SERVICE_DEFS = "ServiceDefs";
    private static final String MODULE_TENANT = "tenant";
    private static final String MASTER_TENANTS = "tenants";
    private static final String MASTER_CITY_MODULE = "citymodule";
    
    @Autowired
    private ObjectMapper mapper;
    
    @Autowired
    private UrbanMCPConfiguration mcpConfiguration;
    
    @Autowired
    private TokenService tokenService;

    private final WebClient webClient;

    /**
     * Constructor for UrbanMCPService.
     * 
     * @param builder WebClient builder for HTTP operations
     */
    public UrbanMCPService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    /**
     * Creates a request info map with authentication token and user information.
     * 
     * @return Map containing authToken and userInfo
     */
    private Map<String, Object> getEmployeeRequestInfo() {
        Map<String, Object> request = new HashMap<>();
        request.put("authToken", tokenService.getCitizenToken());
        request.put("userInfo", tokenService.getUserInfo());
        return request;
    }



    /**
     * Converts a date input to epoch milliseconds.
     * Accepts either yyyy-MM-dd (local midnight in Asia/Kolkata) or a numeric
     * epoch value in seconds or milliseconds. Returns null if the input is blank
     * or cannot be parsed.
     *
     * @param date Date string in yyyy-MM-dd format or epoch (ms/s)
     * @return Epoch time in milliseconds, or null if not parseable
     */
    private Long getEpochTime(String date) {
        if (date == null || date.isBlank()) {
            return null;
        }

        String trimmed = date.trim();

        // Numeric input: epoch seconds or milliseconds
        if (trimmed.chars().allMatch(Character::isDigit)) {
            try {
                long value = Long.parseLong(trimmed);
                // If it looks like seconds (<= 10 digits), convert to ms
                if (trimmed.length() <= 10) {
                    value = value * 1000L;
                }
                return value;
            } catch (NumberFormatException ex) {
                log.warn("Failed to parse epoch value: {}", date);
                return null;
            }
        }

        // ISO-8601 instant or offset datetime, e.g. 2025-01-01T00:00:00Z or 2025-01-01T05:30:00+05:30
        try {
            return java.time.Instant.parse(trimmed).toEpochMilli();
        } catch (Exception ignored) { /* try next */ }

        try {
            return java.time.OffsetDateTime.parse(trimmed).toInstant().toEpochMilli();
        } catch (Exception ignored) { /* try next */ }

        // Date string: yyyy-MM-dd
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            LocalDate localDate = LocalDate.parse(trimmed, formatter);
            return localDate.atStartOfDay(ZoneId.of(TIMEZONE)).toInstant().toEpochMilli();
        } catch (Exception e) {
            log.warn("Invalid date format (expected {}): {}", DATE_FORMAT, date);
            return null;
        }
    }

    @Tool(name="fetch_local_tenants", description="Fetch a list of tenants in the local database (tenant code and display name) that will be used for the search functionality")
    public List<CityTenant> fetchLocalTenantsForSearch(){
        return new ArrayList<>(Arrays.asList(
                new CityTenant("pb.amritsar", "Amritsar"),
                new CityTenant("pb.ludhiana", "Ludhiana"),
                new CityTenant("pb.bhatinda", "Bhatinda"),
                new CityTenant("pb.jalandhar", "Jalandhar"),
                new CityTenant("pb.patiala", "Patiala")
        ));
    }
    /**
     * Searches for complaints within a date range.
     * 
     * @param fromDate Start date in yyyy-MM-dd format (optional)
     * @param toDate End date in yyyy-MM-dd format (optional)
     * @param tenantId Tenant ID for the search
     * @param offset Pagination offset (optional)
     * @param limit Maximum number of results (optional)
     * @return List of complaint summaries
     * @throws IllegalArgumentException if tenantId is null or empty
     * @throws RuntimeException if API call fails
     */
    @Tool(name = "search_complaints_with_dates", description = "Get a list of complaints by dates with the date in yyyy-mm-dd format. Fetch as many records as possible using pagination" + "Call fetch_local_tenants first to identify the tenant codes for which search is being  used")
    public List<ComplaintSummary> getComplaints(String fromDate, String toDate, String tenantId, String offset, String limit) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(LOCAL_BASE_URL + PGR_SEARCH_ENDPOINT)
                    .queryParam("tenantId", tenantId)
                    .queryParamIfPresent("fromDate", Optional.ofNullable(getEpochTime(fromDate)))
                    .queryParamIfPresent("toDate", Optional.ofNullable(getEpochTime(toDate)))
                    .queryParamIfPresent("offset", Optional.ofNullable(offset))
                    .queryParamIfPresent("limit", Optional.ofNullable(limit))
                    .build(true)
                    .toUri();

            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", getEmployeeRequestInfo());

            ServiceResponse response = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(ServiceResponse.class)
                    .block();

            if (response == null) {
                log.warn("Received null response from PGR search API");
                return Collections.emptyList();
            }

            return response.getServiceWrappers().stream()
                    .map(wrapper -> {
                        org.egov.lg.mcp.ccrs.models.Service service = wrapper.getService();
                        return new ComplaintSummary(
                                service.getServiceRequestId(),
                                service.getApplicationStatus(),
                                service.getDescription(),
                                service.getTenantId()
                        );
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Failed to search complaints for tenantId: {}", tenantId, e);
            throw new RuntimeException("Failed to search complaints", e);
        }
    }

    /**
     * Searches for a specific complaint by its number.
     * 
     * @param tenantId Tenant ID for the search
     * @param complaintNumber Complaint number to search for
     * @return List of complaint summaries (should contain at most one item)
     * @throws IllegalArgumentException if tenantId is null or empty
     * @throws RuntimeException if API call fails
     */
    @Tool(name = "search_complaint_by_number", description = "Get a complaint by number" + "Call fetch_local_tenants first to get the tenant code")
    public List<ComplaintSummary> getComplaintByCode(String tenantId, String complaintNumber) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(LOCAL_BASE_URL + PGR_SEARCH_ENDPOINT)
                    .queryParam("tenantId", tenantId)
                    .queryParamIfPresent("serviceRequestId", Optional.ofNullable(complaintNumber))
                    .build(true)
                    .toUri();

            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", getEmployeeRequestInfo());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<ServiceResponse> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    ServiceResponse.class
            );

            if (response.getBody() == null) {
                log.warn("Received null response from PGR search API for complaint: {}", complaintNumber);
                return Collections.emptyList();
            }

            return response.getBody().getServiceWrappers().stream()
                    .map(wrapper -> {
                        org.egov.lg.mcp.ccrs.models.Service service = wrapper.getService();
                        return new ComplaintSummary(
                                service.getServiceRequestId(),
                                service.getApplicationStatus(),
                                service.getDescription(),
                                service.getTenantId()
                        );
                    })
                    .toList();
        } catch (Exception e) {
            log.error("Failed to search complaint by number: {} for tenantId: {}", complaintNumber, tenantId, e);
            throw new RuntimeException("Failed to search complaint by number", e);
        }
    }

    /**
     * Fetches master data list of complaint types and subtypes.
     * 
     * @param tenantId Tenant ID for the search
     * @return JsonNode containing service definitions
     * @throws IllegalArgumentException if tenantId is null or empty
     * @throws RuntimeException if API call fails
     */
    @Tool(name = "list_complaint_categories", description = "Fetch master data list of complaint types and subtypes which can be used to file complaints")
    public JsonNode fetchComplaintCategories(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(DEMO_BASE_URL + MDMS_SEARCH_ENDPOINT)
                    .build(true)
                    .toUri();

            Map<String, Object> payload = new HashMap<>();
            payload.put("RequestInfo", getEmployeeRequestInfo());
            payload.put("MdmsCriteria", buildMdmsCriteria(tenantId));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    uri,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getBody() == null) {
                log.warn("Received null response from MDMS API for tenantId: {}", tenantId);
                return mapper.createObjectNode();
            }

            JsonNode root = mapper.readTree(response.getBody());
            return root.path("MdmsRes")
                    .path(MODULE_RAINMAKER_PGR)
                    .path(MASTER_SERVICE_DEFS);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON response from MDMS API for tenantId: {}", tenantId, e);
            throw new RuntimeException("Failed to parse complaint categories response", e);
        } catch (Exception e) {
            log.error("Failed to fetch complaint categories for tenantId: {}", tenantId, e);
            throw new RuntimeException("Failed to fetch complaint categories", e);
        }
    }

    /**
     * Record representing a city tenant with code and display name.
     */
    public record CityTenant(String code, String name) {}

    /**
     * Fetches city-level tenants for complaint filing.
     * This is typically the first step when filing a complaint.
     * 
     * @return List of city tenants with their codes and display names
     * @throws RuntimeException if API call fails
     */
    @Tool(
            name = "fetch_city_tenants",
            description = "This is the first thing a user has to select while filing a complaint. Fetch city-level tenants (e.g., pg.citya) with display names for tenant pg which is the state level tenant"
    )
    public List<CityTenant> fetchCityTenants() {
        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(DEMO_BASE_URL + MDMS_SEARCH_ENDPOINT)
                    .build(true)
                    .toUri();

            Map<String, Object> requestInfo = new HashMap<>();
            requestInfo.put("authToken", tokenService.getCitizenToken());
            requestInfo.put("userInfo", tokenService.getUserInfo());

            Map<String, Object> mdmsCriteria = new HashMap<>();
            mdmsCriteria.put("tenantId", "pg");

            Map<String, Object> mdTenant = new HashMap<>();
            mdTenant.put("moduleName", MODULE_TENANT);
            mdTenant.put("masterDetails", List.of(
                    Map.of("name", MASTER_TENANTS),
                    Map.of("name", MASTER_CITY_MODULE)
            ));

            mdmsCriteria.put("moduleDetails", List.of(mdTenant));

            Map<String, Object> body = new HashMap<>();
            body.put("RequestInfo", requestInfo);
            body.put("MdmsCriteria", mdmsCriteria);

            JsonNode root = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            List<CityTenant> result = new ArrayList<>();
            if (root != null) {
                JsonNode tenants = root.path("MdmsRes").path(MODULE_TENANT).path(MASTER_TENANTS);
                if (tenants.isArray()) {
                    for (JsonNode tenant : tenants) {
                        String code = tenant.path("code").asText(null);
                        if (code == null) continue;
                        
                        // Filter for city-level tenants (state.city format)
                        if (!code.contains(".") || !code.startsWith("pg.")) continue;

                        String display = tenant.path("city").path("name").asText(null);
                        if (display == null || display.isBlank()) {
                            display = tenant.path("name").asText(code);
                        }
                        result.add(new CityTenant(code, display));
                    }
                }
            }

            return result;
        } catch (Exception e) {
            log.error("Failed to fetch city tenants", e);
            throw new RuntimeException("Failed to fetch city tenants", e);
        }
    }

    /**
     * Record representing a locality with code and name.
     */
    public record Locality(String code, String name) {}

    /**
     * Fetches localities for a given city-level tenant.
     * 
     * @param tenantId City-level tenant ID (e.g., 'pg.citya')
     * @return List of localities with their codes and names
     * @throws IllegalArgumentException if tenantId is null or empty
     * @throws RuntimeException if API call fails
     */
    @Tool(
            name = "fetch_address_locality",
            description = "Return locality {code,name} for a city-level tenantId (e.g., 'pg.citya'). " +
                    "Use fetch_city_tenants to list cities under a state and pick the tenantId."
    )
    public List<Locality> fetchAddressLocality(String tenantId) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }

        try {
            URI uri = UriComponentsBuilder
                    .fromUriString(DEMO_BASE_URL + LOCATION_SEARCH_ENDPOINT)
                    .queryParam("hierarchyTypeCode", HIERARCHY_TYPE_ADMIN)
                    .queryParam("boundaryType", BOUNDARY_TYPE_LOCALITY)
                    .queryParam("tenantId", tenantId)
                    .build(false)
                    .toUri();

            Map<String, Object> payload = Map.of(
                    "RequestInfo", Map.of(
                            "apiId", "Rainmaker",
                            "authToken", tokenService.getCitizenToken(),
                            "msgId", System.currentTimeMillis() + "|en_IN",
                            "plainAccessRequest", Map.of()
                    )
            );

            JsonNode root = webClient.post()
                    .uri(uri)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8")
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .exchangeToMono(resp -> {
                        if (resp.statusCode().is2xxSuccessful()) {
                            return resp.bodyToMono(JsonNode.class);
                        }
                        return resp.bodyToMono(String.class).flatMap(err ->
                                Mono.error(new RuntimeException("Location API error " + resp.statusCode() + " — " + err)));
                    })
                    .block();

            List<Locality> result = new ArrayList<>();
            if (root != null) {
                JsonNode tenantBoundary = root.path("TenantBoundary");
                if (tenantBoundary.isArray() && !tenantBoundary.isEmpty()) {
                    JsonNode boundaries = tenantBoundary.get(0).path("boundary");
                    if (boundaries.isArray()) {
                        boundaries.forEach(boundary -> result.add(new Locality(
                                boundary.path("code").asText(),
                                boundary.path("name").asText()
                        )));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch localities for tenantId: {}", tenantId, e);
            throw new RuntimeException("Failed to fetch localities", e);
        }
    }

    /**
     * Convenience helper to find a locality code by exact (case-insensitive) name.
     * 
     * @param tenantId Tenant ID for the search
     * @param name Locality name to search for
     * @return Optional containing the locality code if found
     */
    public Optional<String> findLocalityCodeByName(String tenantId, String name) {
        if (tenantId == null || tenantId.trim().isEmpty() || name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        
        return fetchAddressLocality(tenantId).stream()
                .filter(locality -> locality.name().equalsIgnoreCase(name.trim()))
                .map(Locality::code)
                .findFirst();
    }

    /**
     * Creates a citizen complaint with the provided details.
     * 
     * @param tenantId Tenant ID for the complaint
     * @param serviceCode Service code for the complaint type
     * @param localityCode Locality code where the complaint is filed
     * @return Complaint summary of the created complaint
     * @throws IllegalArgumentException if any required parameter is null or empty
     * @throws RuntimeException if API call fails
     */
    @Tool(
            name = "create_citizen_complaint_with_code",
            description = "Create a PGR complaint in pg.citya (Victoria). Inputs: tenantId, serviceCode, localityCode. " +
                    "Use fetch_city_tenants first to fetch all city tenants & then fetch_address_locality to get the localityCode based on the output from fetch_city_tenants"
    )
    public ComplaintSummary createCitizenComplaint(String tenantId, String serviceCode, String localityCode) {
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalArgumentException("TenantId cannot be null or empty");
        }
        if (serviceCode == null || serviceCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ServiceCode cannot be null or empty");
        }
        if (localityCode == null || localityCode.trim().isEmpty()) {
            throw new IllegalArgumentException("LocalityCode cannot be null or empty");
        }

        try {
            Map<String, Object> payload = new HashMap<>();

            Map<String, Object> service = new HashMap<>();
            service.put("tenantId", tenantId);
            service.put("serviceCode", serviceCode);
            service.put("source", SOURCE_WEB);
            
            Map<String, Object> address = new HashMap<>();
            address.put("locality", Map.of("code", localityCode));
            service.put("address", address);
            
            payload.put("service", service);
            payload.put("workflow", Map.of("action", WORKFLOW_ACTION_APPLY));
            payload.put("RequestInfo", Map.of("authToken", tokenService.getCitizenToken()));

            URI uri = UriComponentsBuilder
                    .fromUriString(DEMO_BASE_URL + PGR_CREATE_ENDPOINT)
                    .build(true)
                    .toUri();

            ServiceResponse response = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .exchangeToMono(resp -> {
                        if (resp.statusCode().is2xxSuccessful()) {
                            return resp.bodyToMono(ServiceResponse.class);
                        }
                        return resp.createException().flatMap(Mono::error);
                    })
                    .block();

            if (response == null || response.getServiceWrappers() == null || response.getServiceWrappers().isEmpty()) {
                throw new RuntimeException("PGR create returned empty response");
            }

            org.egov.lg.mcp.ccrs.models.Service serviceData = response.getServiceWrappers().get(0).getService();
            return new ComplaintSummary(
                    serviceData.getServiceRequestId(),
                    serviceData.getApplicationStatus(),
                    serviceData.getDescription(),
                    serviceData.getTenantId()
            );
        } catch (WebClientResponseException ex) {
            log.error("PGR create failed: {} {} body={}",
                    ex.getStatusCode(), ex.getStatusText(), ex.getResponseBodyAsString());
            throw new RuntimeException("Failed to create complaint: " + ex.getStatusText(), ex);
        } catch (Exception e) {
            log.error("Failed to create complaint for tenantId: {}, serviceCode: {}, localityCode: {}", 
                    tenantId, serviceCode, localityCode, e);
            throw new RuntimeException("Failed to create complaint", e);
        }
    }

    /**
     * Builds MDMS criteria for service definitions.
     * 
     * @param stateTenant State-level tenant ID
     * @return Map containing MDMS criteria
     * @throws IllegalArgumentException if stateTenant is null or empty
     */
    public Map<String, Object> buildMdmsCriteria(String stateTenant) {
        if (stateTenant == null || stateTenant.trim().isEmpty()) {
            throw new IllegalArgumentException("StateTenant cannot be null or empty");
        }

        Map<String, Object> masterDetail = new HashMap<>();
        masterDetail.put("name", MASTER_SERVICE_DEFS);

        Map<String, Object> moduleDetail = new HashMap<>();
        moduleDetail.put("moduleName", MODULE_RAINMAKER_PGR);
        moduleDetail.put("masterDetails", Collections.singletonList(masterDetail));

        Map<String, Object> mdmsCriteria = new HashMap<>();
        mdmsCriteria.put("tenantId", stateTenant);
        mdmsCriteria.put("moduleDetails", Collections.singletonList(moduleDetail));

        return mdmsCriteria;
    }

    /**
     * Post-construct initialization method.
     * Currently empty but available for future initialization needs.
     */
    @PostConstruct
    public void init() {
        log.info("UrbanMCPService initialized successfully");
    }
}
