package com.sandbox.gateway;

import com.sandbox.gateway.filter.TokenVerificationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class GatewayApplication {

    private final TokenVerificationFilter tokenVerificationFilter;

    public GatewayApplication(TokenVerificationFilter tokenVerificationFilter) {
        this.tokenVerificationFilter = tokenVerificationFilter;
    }

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

    @Bean
    @Profile("prod")
    public RouteLocator prodRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service-register", r -> r
                .path("/api/register")
                .filters(f -> f.stripPrefix(0))
                .uri("http://user-service-prod:8081"))
            .route("user-service-profile", r -> r
                .path("/api/profile")
                .filters(f -> f.filter(tokenVerificationFilter.apply(new Object())).stripPrefix(0))
                .uri("http://user-service-prod:8081"))
            .route("user-service-privileges", r -> r
                .path("/api/privileges")
                .filters(f -> f.filter(tokenVerificationFilter.apply(new Object())).stripPrefix(0))
                .uri("http://user-service-prod:8081"))
            .route("auth-login", r -> r
                .path("/api/login")
                .filters(f -> f.stripPrefix(0))
                .uri("http://auth-service-prod:8083"))
            .route("auth-verify", r -> r
                .path("/api/verify")
                .filters(f -> f.stripPrefix(0))
                .uri("http://auth-service-prod:8083"))
            .route("qris-service", r -> r
                .path("/api/qris/**")
                .filters(f -> f.filter(tokenVerificationFilter.apply(new Object())).stripPrefix(0))
                .uri("http://qris-service-prod:8086"))
            .route("cashin-service", r -> r
                .path("/api/cashin/**")
                .filters(f -> f.filter(tokenVerificationFilter.apply(new Object())).stripPrefix(0))
                .uri("http://cashin-service-prod:8088"))
            .build();
    }

    @Bean
    @Profile("sandbox")
    public RouteLocator sandboxRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("sandbox-auth-login", r -> r
                .path("/api/login")
                .filters(f -> f.stripPrefix(0))
                .uri("http://auth-service-sandbox:8084"))
            .route("sandbox-sync", r -> r
                .path("/api/sync")
                .filters(f -> f.stripPrefix(0))
                .uri("http://user-service-sandbox:8082"))
            .route("sandbox-user-service-profile", r -> r
                .path("/api/profile")
                .filters(f -> f.filter(tokenVerificationFilter.apply(new Object())).stripPrefix(0))
                .uri("http://user-service-sandbox:8082"))
            .route("sandbox-user-service-privileges", r -> r
                .path("/api/privileges")
                .filters(f -> f.filter(tokenVerificationFilter.apply(new Object())).stripPrefix(0))
                .uri("http://user-service-sandbox:8082"))
            .route("sandbox-auth-verify", r -> r
                .path("/api/verify")
                .filters(f -> f.stripPrefix(0))
                .uri("http://auth-service-sandbox:8084"))
            .route("sandbox-qris-service", r -> r
                .path("/api/qris/**")
                .filters(f -> f.filter(tokenVerificationFilter.apply(new Object())).stripPrefix(0))
                .uri("http://qris-service-sandbox:8087"))
            .route("sandbox-cashin-service", r -> r
                .path("/api/cashin/**")
                .filters(f -> f.filter(tokenVerificationFilter.apply(new Object())).stripPrefix(0))
                .uri("http://cashin-service-sandbox:8089"))
            .build();
    }
}
