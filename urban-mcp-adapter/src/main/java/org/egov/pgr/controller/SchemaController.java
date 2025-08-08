package org.egov.pgr.controller;

import lombok.RequiredArgsConstructor;
import org.egov.pgr.service.SchemaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pgr-service/v1")
@RequiredArgsConstructor
public class SchemaController {

    private final SchemaService schemaService;

    @GetMapping("/describe")
    public ResponseEntity<Object> describe() {
        return ResponseEntity.ok(schemaService.getServiceRequestSchema());
    }

    @GetMapping("/schemas")
    public ResponseEntity<Object> listSchemas() {
        return ResponseEntity.ok(schemaService.listAvailableSchemas());
    }

    @GetMapping("/schema/{model}")
    public ResponseEntity<Object> getSchema(@PathVariable String model) {
        return ResponseEntity.ok(schemaService.getSchemaByName(model));
    }

    @GetMapping("/context")
    public ResponseEntity<Object> context() {
        return ResponseEntity.ok(schemaService.getContext());
    }

    @GetMapping("/tools")
    public ResponseEntity<Object> tools() {
        return ResponseEntity.ok(schemaService.getTools());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
