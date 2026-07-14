package com.sandbox.gateway.filter;

import com.sandbox.gateway.security.JwtTokenProvider;
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
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${gateway.environment:production}")
    private String gatewayEnvironment;

    public TokenVerificationFilter(@Value("${auth.service.url}") String authServiceUrl,
                                    JwtTokenProvider jwtTokenProvider) {
        this.webClient = WebClient.create(authServiceUrl);
        this.jwtTokenProvider = jwtTokenProvider;
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

            String tokenEnv = jwtTokenProvider.getEnvironmentFromToken(token);
            if (!gatewayEnvironment.equals(tokenEnv)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

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
