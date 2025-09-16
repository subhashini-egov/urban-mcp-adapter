package org.egov.lg.mcp.ccrs.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.tracer.annotations.CustomSafeHtml;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * BPA application object to capture the details of land, land owners, and address of the land.
 */
@Validated
//@javax.annotation.Generated(value = "org.egov.codegen.SpringBootCodegen", date = "2020-07-15T11:35:33.568+05:30")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Workflow   {
        @CustomSafeHtml
        @JsonProperty("action")
        private String action = null;

        @JsonProperty("assignes")
        @Valid
        private List<String> assignes = null;

        @CustomSafeHtml
        @JsonProperty("comments")
        private String comments = null;

        @JsonProperty("verificationDocuments")
        @Valid
        private List<Document> verificationDocuments = null;


        public Workflow addAssignesItem(String assignesItem) {
            if (this.assignes == null) {
            this.assignes = new ArrayList<>();
            }
        this.assignes.add(assignesItem);
        return this;
        }

        public Workflow addVarificationDocumentsItem(Document verificationDocumentsItem) {
            if (this.verificationDocuments == null) {
            this.verificationDocuments = new ArrayList<>();
            }
        this.verificationDocuments.add(verificationDocumentsItem);
        return this;
        }

}

