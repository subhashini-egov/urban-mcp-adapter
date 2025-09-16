package org.egov.lg.mcp.ccrs.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Response to the service request
 */
@Validated
//@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ServiceResponse   {
        @JsonProperty("responseInfo")
        private ResponseInfo responseInfo = null;

        @JsonProperty("ServiceWrappers")
        private List<ServiceWrapper> serviceWrappers = null;
        
        @JsonProperty("complaintsResolved")
        private int complaintsResolved;

        @JsonProperty("averageResolutionTime")
        private int averageResolutionTime;

        @JsonProperty("complaintTypes")
        private int complaintTypes;


}

