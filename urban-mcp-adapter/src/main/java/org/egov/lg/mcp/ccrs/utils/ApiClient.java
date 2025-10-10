package org.egov.lg.mcp.ccrs.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ApiClient {

    private final WebClient webClient;
    private final TokenService tokenService;

    public ApiClient(WebClient.Builder builder, TokenService tokenService) {
        this.webClient = builder.build();
        this.tokenService = tokenService;
    }

   // public JsonNode getProtectedResource(String url) {
//        String token = tokenService.Citizen();
//        return webClient.get()
//                .uri("https://api.example.com/protected")
//                .header("Authorization", "Bearer " + token)
//                .retrieve()
//                .bodyToMono(JsonNode.class)
//                .block();
  //  }
}

