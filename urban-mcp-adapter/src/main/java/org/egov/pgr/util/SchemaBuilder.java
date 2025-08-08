package org.egov.pgr.util;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SchemaBuilder {

    public Map<String, Object> buildServiceRequestSchema() {
        return Map.of(
            "@type", "Schema",
            "name", "ServiceRequest",
            "description", "A grievance raised by a citizen in the PGR system",
            "version", "1.0.0",
            "parameters", Map.of(
                "type", "object",
                "required", List.of("serviceCode", "tenantId"),
                "properties", Map.of(
                    "serviceRequestId", Map.of(
                        "type", "string",
                        "description", "Auto-generated grievance ID"
                    ),
                    "serviceCode", Map.of(
                        "type", "string",
                        "enum", List.of("Garbage", "StreetLight", "WaterLeak"),
                        "description", "Type of grievance"
                    ),
                    "description", Map.of(
                        "type", "string",
                        "description", "Details of the issue"
                    ),
                    "tenantId", Map.of(
                        "type", "string",
                        "description", "City or municipal ID"
                    )
                )
            )
        );
    }
}
