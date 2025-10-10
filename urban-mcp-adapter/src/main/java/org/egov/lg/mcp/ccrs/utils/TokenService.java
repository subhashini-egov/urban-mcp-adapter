package org.egov.lg.mcp.ccrs.utils;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import org.egov.lg.mcp.ccrs.UrbanMCPConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class TokenService {

    private final WebClient webClient;
    private final AtomicReference<String> citizenTokenRef = new AtomicReference<>();
    @Getter
    JsonNode userInfo;

    @Autowired
    private UrbanMCPConfiguration configuration;

    public TokenService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public String getCitizenToken() {
        // refresh if missing or expired (buffer 10 seconds)
        if (citizenTokenRef.get() == null) {
            refreshCitizenToken();
        }
        return citizenTokenRef.get();
    }

    public void refreshCitizenToken() {
        // 1) Build URL with params
        URI uri = UriComponentsBuilder
                .fromHttpUrl(configuration.getDemoBaseUrl() + "/user/oauth/token")
                .build(false) // keep encoded if needed
                .toUri();

        // 2) Call API & parse
        JsonNode resp = webClient.post()
                .uri(uri)
                .header("Authorization", "Basic ZWdvdi11c2VyLWNsaWVudDo=")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromFormData("username", configuration.getAuthUsername())
                        .with("password", configuration.getAuthPassword())
                        .with("tenantId", configuration.getAuthTenantId())
                        .with("userType", configuration.getAuthUserType())
                        .with("scope", configuration.getAuthScope())
                        .with("grant_type", configuration.getAuthGrantType()))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException("Auth call failed", ex)))
                .block(); // blocking here for simplicity (non-reactive app)

        // 3) Store token + expiry
        if (resp != null && resp.has("access_token")) {
            citizenTokenRef.set(sanitizeToken(resp.path("access_token").asText()));
            userInfo = resp.get("UserRequest"); // store only that object
        }
    }

    private String sanitizeToken(String raw) {
        if (raw == null) throw new IllegalStateException("Citizen token is null");
        String t = raw.trim();
        // If someone parsed token with JsonNode#toString(), it will be quoted → strip quotes
        if (t.length() >= 2 && t.startsWith("\"") && t.endsWith("\"")) {
            t = t.substring(1, t.length() - 1);
        }
        // No "Bearer " prefix inside RequestInfo
        if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
            t = t.substring(7).trim();
        }
        return t;
    }
}

