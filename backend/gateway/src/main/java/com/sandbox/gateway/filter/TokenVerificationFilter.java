package com.sandbox.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TokenVerificationFilter extends AbstractGatewayFilterFactory<Object> {

    private final WebClient webClient;

    public TokenVerificationFilter(@Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = WebClient.create(authServiceUrl);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            return webClient.get()
                .uri("/api/verify")
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(VerifyResponse.class)
                .flatMap(response -> {
                    ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", response.getUserId().toString())
                        .header("X-User-Email", response.getEmail())
                        .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .onErrorResume(e -> {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
        };
    }

    public static class VerifyResponse {
        private Long userId;
        private String email;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}
