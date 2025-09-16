package org.egov.lg.mcp.ccrs.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.validation.annotation.Validated;

/**
 * minimal representation of the Roles in the system to be carried along in UserInfo with RequestHeader meta data. Actual authorization service to extend this to have more role related attributes 
 */
@Validated
//@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role   {
        @JsonProperty("name")
        private String name = null;

        @JsonProperty("code")
        private String code = null;

        @JsonProperty("tenantId")
        private String tenantId = null;

        @JsonProperty("description")
        private String description = null;


}

