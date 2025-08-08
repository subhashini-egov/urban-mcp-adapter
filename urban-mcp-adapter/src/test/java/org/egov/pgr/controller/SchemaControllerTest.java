package org.egov.pgr.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SchemaController.class)
public class SchemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testDescribeEndpoint() throws Exception {
        mockMvc.perform(get("/pgr-service/v1/describe"))
               .andExpect(status().isOk());
    }

    @Test
    void testSchemasEndpoint() throws Exception {
        mockMvc.perform(get("/pgr-service/v1/schemas"))
               .andExpect(status().isOk());
    }

    @Test
    void testSchemaByNameEndpoint() throws Exception {
        mockMvc.perform(get("/pgr-service/v1/schema/ServiceRequest"))
               .andExpect(status().isOk());
    }

    @Test
    void testContextEndpoint() throws Exception {
        mockMvc.perform(get("/pgr-service/v1/context"))
               .andExpect(status().isOk());
    }

    @Test
    void testToolsEndpoint() throws Exception {
        mockMvc.perform(get("/pgr-service/v1/tools"))
               .andExpect(status().isOk());
    }

    @Test
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/pgr-service/v1/health"))
               .andExpect(status().isOk());
    }
}
