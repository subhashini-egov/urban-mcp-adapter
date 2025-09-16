package org.egov.lg.mcp.ccrs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.egov.lg.mcp.ccrs.models.ComplaintSummary;
import org.egov.lg.mcp.ccrs.models.ServiceRequest;
import org.egov.lg.mcp.ccrs.models.ServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UrbanMCPService {
    private static final Logger log = LoggerFactory.getLogger(UrbanMCPService.class);
    @Autowired
    private ObjectMapper mapper;

    private Map<String,Object> getRequestInfo(){

        Map<String, Object> request = new HashMap<>();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 298);
        userInfo.put("uuid", "bcdffd02-283a-446e-8b48-0188392f0ba1");
        userInfo.put("userName", "CSR");
        userInfo.put("name", "CSR USER");
        userInfo.put("mobileNumber", "9999999901");
        userInfo.put("emailId", null);
        userInfo.put("locale", null);
        userInfo.put("type", "EMPLOYEE");

        List<Map<String, Object>> roles = new ArrayList<>();

        Map<String, Object> role1 = new HashMap<>();
        role1.put("name", "Complainant");
        role1.put("code", "CSR");
        role1.put("tenantId", "statea.citya");
        roles.add(role1);

        Map<String, Object> role2 = new HashMap<>();
        role2.put("name", "PGR Viewer role");
        role2.put("code", "PGR_VIEWER");
        role2.put("tenantId", "statea.citya");
        roles.add(role2);

        userInfo.put("roles", roles);
        userInfo.put("active", true);
        userInfo.put("tenantId", "statea.citya");
        userInfo.put("permanentCity", null);

        request.put("userInfo", userInfo);
        request.put("authToken","c53552cc-00cd-4bf4-9903-b69471afb398");
        return request;

    }



    private long getEpochTime(String date){
        // Convert dates to epoch milliseconds
        Long epochTime = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (date != null) {
            LocalDate from = LocalDate.parse(date, formatter);
            epochTime = from.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant().toEpochMilli();
        }

       return epochTime;
    }

    @Tool(name="search_complaints_with_dates", description="Get a list of complaints by dates")
    public List<ComplaintSummary> getComplaints(String fromDate, String toDate, String tenantId, String offset, String limit){
        // Build the query parameter string
        StringBuilder urlBuilder = new StringBuilder("http://localhost:8090/pgr-services/v2/request/_search?");
        urlBuilder.append("tenantId=").append(tenantId);
        if (fromDate != null) urlBuilder.append("&fromDate=").append(getEpochTime(fromDate));
        if (toDate != null) urlBuilder.append("&toDate=").append(getEpochTime(toDate));
        if (offset!=null) urlBuilder.append("&offset=").append(offset);
        if (limit!=null) urlBuilder.append("&limit=").append(limit);
        String urlWithParams = urlBuilder.toString();

        // Prepare RequestInfo body
        Map<String, Object> payload = new HashMap<>();
        payload.put("RequestInfo", getRequestInfo()); // Replace with actual method to get RequestInfo

        // Prepare headers and entity
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Call API
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ServiceResponse> response = restTemplate.exchange(
                urlWithParams,
                HttpMethod.POST,
                entity,
                ServiceResponse.class
        );

        assert response.getBody() != null;
        List<ComplaintSummary> complaints = response.getBody().getServiceWrappers().stream()
                .map(wrapper -> {
                    org.egov.lg.mcp.ccrs.models.Service s = wrapper.getService();
                    return new ComplaintSummary(
                            s.getServiceRequestId(),
                            s.getApplicationStatus(),
                            s.getDescription(),
                            s.getTenantId()
                    );
                })
                .toList();
        return complaints;
    }

    @Tool(name="search_complaint_by_number", description="Get a complaint by number")
    public List<ComplaintSummary> getComplaintByCode(String tenantId, String complaintNumber)
    {
        // Build the query parameter string
        StringBuilder urlBuilder = new StringBuilder("http://localhost:8090/pgr-services/v2/request/_search?");
        urlBuilder.append("tenantId=").append(tenantId);
        if (complaintNumber != null) urlBuilder.append("&serviceRequestId=").append(complaintNumber);
        String urlWithParams = urlBuilder.toString();

        // Prepare RequestInfo body
        Map<String, Object> payload = new HashMap<>();
        payload.put("RequestInfo", getRequestInfo()); // Replace with actual method to get RequestInfo

        // Prepare headers and entity
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Call API
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<ServiceResponse> response = restTemplate.exchange(
                urlWithParams,
                HttpMethod.POST,
                entity,
                ServiceResponse.class
        );

        assert response.getBody() != null;
        List<ComplaintSummary> complaints = response.getBody().getServiceWrappers().stream()
                .map(wrapper -> {
                    org.egov.lg.mcp.ccrs.models.Service s = wrapper.getService();
                    return new ComplaintSummary(
                            s.getServiceRequestId(),
                            s.getApplicationStatus(),
                            s.getDescription(),
                            s.getTenantId()
                    );
                })
                .toList();
        return complaints;
    }

    @Tool(name="list_complaint_categories", description="List complaint categories")
    public JsonNode fetchComplaintCategories(String tenantId) throws JsonProcessingException {
        StringBuilder urlBuilder = new StringBuilder("https://pgr-demo.digit.org/mdms-v2/v1/_search");
        // Prepare RequestInfo body
        Map<String, Object> payload = new HashMap<>();
        payload.put("RequestInfo", getRequestInfo()); // Replace with actual method to get RequestInfo

        Map<String, Object> mdmsCriteria = buildMdmsCriteria("statea");
        payload.put("MdmsCriteria", mdmsCriteria);

        // Prepare headers and entity
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        // Call API
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                urlBuilder.toString(),
                HttpMethod.POST,
                entity,
                String.class
        );
        JsonNode root = mapper.readTree(String.valueOf(response.getBody()));
        //System.out.println(root.path("MdmsRes").get(0).path("serviceRequestId").asText());
        //JsonNode root = mapper.readTree(json);

        JsonNode serviceDefs = root.path("MdmsRes")
                .path("RAINMAKER-PGR")
                .path("ServiceDefs");
        return serviceDefs;
//        HashMap<String, Object> serviceCodes
//        if (serviceDefs.isArray()) {
//            for (JsonNode def : serviceDefs) {
//                String name = def.path("name").asText();
//                String serviceCode = def.path("serviceCode").asText();
//                boolean active = def.path("active").asBoolean();
//                int slaHours = def.path("slaHours").asInt();
//                String department = def.path("department").asText();
//
//                System.out.printf("Service: %s (%s), SLA: %d hours, Dept: %s, Active: %s%n",
//                        name, serviceCode, slaHours, department, active);
//            }
//        }
    }


    @Tool(name="create_citizen_complaint", description="Create a complaint")
    //@PromptTemplate("Choose a complaint category.")
    public ServiceRequest createComplaint(String tenantId,
                                          String serviceTypeCode
                                          )
    {
        return null;
    }

    public Map<String, Object> buildMdmsCriteria(String stateTenant) {
        // MasterDetail map
        Map<String, Object> masterDetail = new HashMap<>();
        masterDetail.put("name", "ServiceDefs");

        // ModuleDetail map
        Map<String, Object> moduleDetail = new HashMap<>();
        moduleDetail.put("moduleName", "RAINMAKER-PGR");
        moduleDetail.put("masterDetails", Collections.singletonList(masterDetail));

        // MdmsCriteria map
        Map<String, Object> mdmsCriteria = new HashMap<>();
        mdmsCriteria.put("tenantId", stateTenant);
        mdmsCriteria.put("moduleDetails", Collections.singletonList(moduleDetail));

        return mdmsCriteria;
    }

    @PostConstruct
    public void init() {
//        courses.addAll(List.of(
//                new Course("DIGIT Core", "https://core.digit.org"),
//                new Course("DIGIT Docs", "https://docs.digit.org")
//        ));
    }


}
