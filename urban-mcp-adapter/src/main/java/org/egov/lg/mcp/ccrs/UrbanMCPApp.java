package org.egov.lg.mcp.ccrs;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class UrbanMCPApp {

    public static void main(String[] args) {

        SpringApplication.run(UrbanMCPApp.class, args);
    }

    @Bean
    public List<ToolCallback> courseTools(UrbanMCPService service) {
        return List.of(ToolCallbacks.from(service));
    }
}
