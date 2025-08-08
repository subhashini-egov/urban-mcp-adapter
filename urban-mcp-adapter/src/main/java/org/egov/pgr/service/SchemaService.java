package org.egov.pgr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.pgr.util.SchemaBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchemaService {

    private static final String SCHEMAS_BASE_PATH = "schemas/";
    private static final String TOOLS_BASE_PATH = "tools/";

    private final SchemaBuilder schemaBuilder;
    private final ObjectMapper objectMapper;

    public Object getServiceRequestSchema() {
        try {
            return loadJsonFile(SCHEMAS_BASE_PATH + "ServiceRequest-1.0.0.schema.json");
        } catch (IOException e) {
            log.error("Failed to load service request schema", e);
            return createErrorResponse("Failed to load service request schema", e);
        }
    }

    public Object listAvailableSchemas() {
        try {
            return Map.of("schemas", new String[]{"ServiceRequest"});
        } catch (Exception e) {
            log.error("Failed to list available schemas", e);
            return createErrorResponse("Failed to list available schemas", e);
        }
    }

    public Object getSchemaByName(String model) {
        try {
            String schemaFileName = model + "-1.0.0.schema.json";
            return loadJsonFile(SCHEMAS_BASE_PATH + schemaFileName);
        } catch (IOException e) {
            log.error("Failed to load schema for model: " + model, e);
            return createErrorResponse("Failed to load schema for model: " + model, e);
        }
    }

    public Object getContext() {
        return Map.of("@context", "https://schema.anthropic.com/v1");
    }

    public Object getTools() {
        try {
            return loadJsonFile(TOOLS_BASE_PATH + "PGRServiceTool-MCP.json");
        } catch (IOException e) {
            log.error("Failed to load tools", e);
            return createErrorResponse("Failed to load tools", e);
        }
    }

    private JsonNode loadJsonFile(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IOException("Resource not found: " + path);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);
        }
    }

    private Map<String, Object> createErrorResponse(String message, Exception e) {
        return Map.of(
            "error", message,
            "details", e.getMessage()
        );
    }

}
