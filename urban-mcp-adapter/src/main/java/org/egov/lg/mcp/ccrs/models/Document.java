package org.egov.lg.mcp.ccrs.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.tracer.annotations.CustomSafeHtml;
import org.springframework.validation.annotation.Validated;

/**
 * This object holds list of documents attached during the transaciton for a property
 */
@Validated
//@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Document   {
        @CustomSafeHtml
        @JsonProperty("id")
        private String id = null;

        @CustomSafeHtml
        @JsonProperty("documentType")
        private String documentType = null;

        @CustomSafeHtml
        @JsonProperty("fileStoreId")
        private String fileStoreId = null;

        @CustomSafeHtml
        @JsonProperty("documentUid")
        private String documentUid = null;

        @JsonProperty("additionalDetails")
        private Object additionalDetails = null;


}

