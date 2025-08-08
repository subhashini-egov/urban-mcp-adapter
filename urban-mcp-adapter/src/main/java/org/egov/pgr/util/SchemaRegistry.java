package org.egov.pgr.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
public class SchemaRegistry {

    private final Map<String, Supplier<Map<String, Object>>> registry = new HashMap<>();

    public SchemaRegistry(SchemaBuilder builder) {
        registry.put("ServiceRequest:1.0.0", builder::buildServiceRequestSchema);
    }

    public Map<String, Object> getSchema(String name, String version) {
        return registry.getOrDefault(name + ":" + version, () -> Map.of("error", "Schema not found")).get();
    }

    public Map<String, Supplier<Map<String, Object>>> getRegistry() {
        return registry;
    }
}
