package org.egov.lg.mcp.ccrs.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.egov.common.contract.workflow.ProcessInstance;
import org.egov.tracer.annotations.CustomSafeHtml;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Instance of Service request raised for a particular service. As per extension propsed in the Service definition \&quot;attributes\&quot; carry the input values requried by metadata definition in the structure as described by the corresponding schema.  * Any one of &#39;address&#39; or &#39;(lat and lang)&#39; or &#39;addressid&#39; is mandatory 
 */
@Validated
//@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Service   {

        @JsonProperty("active")
        private boolean active = true;

        @JsonProperty("citizen")
        private User citizen = null;

        @CustomSafeHtml
        @JsonProperty("id")
        private String id = null;

        @NotNull
        @CustomSafeHtml
        @JsonProperty("tenantId")
        private String tenantId = null;

        @NotNull
        @CustomSafeHtml
        @JsonProperty("serviceCode")
        private String serviceCode = null;

        @CustomSafeHtml
        @JsonProperty("serviceRequestId")
        private String serviceRequestId = null;

        @CustomSafeHtml
        @JsonProperty("description")
        private String description = null;

        @CustomSafeHtml
        @JsonProperty("accountId")
        private String accountId = null;

        @Max(5)
        @Min(1)
        @JsonProperty("rating")
        private Integer rating ;

        @JsonProperty("additionalDetail")
        private Object additionalDetail = null;

        @CustomSafeHtml
        @JsonProperty("applicationStatus")
        private String applicationStatus = null;

        @NotNull
        @CustomSafeHtml
        @JsonProperty("source")
        private String source = null;

        @Valid
        @NotNull
        @JsonProperty("address")
        private Address address = null;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails = null;

        @JsonProperty("processInstance")
        private ProcessInstance processInstance;

}

