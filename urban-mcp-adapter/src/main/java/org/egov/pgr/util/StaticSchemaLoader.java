package org.egov.pgr.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * This class is no longer needed as schema registration is now handled by SchemaRegistry
 * through SchemaBuilder. This class is kept for backward compatibility but does nothing.
 */
@Component
@RequiredArgsConstructor
public class StaticSchemaLoader {

    @PostConstruct
    public void loadSchemas() {
        // Schema registration is now handled by SchemaRegistry through SchemaBuilder
        // This method is kept for backward compatibility
    }
}
