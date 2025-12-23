package com.example.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class UserIdHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
         return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication())
                .filter(authentication -> authentication instanceof JwtAuthenticationToken)
                .map(authentication -> (JwtAuthenticationToken) authentication)
                .flatMap(jwtAuth -> {
                    String userId = extractUserId(jwtAuth);
                    String organizationId = extractOrganizationId(jwtAuth);

                    if (userId != null) {
                        log.debug("Extracted userId from JWT: {}", userId);

                        ServerWebExchange modifiedExchange = exchange.mutate()
                                .request(builder -> {
                                    builder.header("X-User-Id", userId);
                                    if (organizationId != null && !organizationId.isEmpty()) {
                                        builder.header("X-Organization-Id", organizationId);
                                        log.info("Added X-Organization-Id header: {}", organizationId);
                                    }
                                })
                                .build();
                        return chain.filter(modifiedExchange);
                    } else {
                        log.warn("Could not extract userId from JWT");
                        return chain.filter(exchange);
                    }
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private String extractUserId(JwtAuthenticationToken jwtAuth) {
        // 1. Пробуем subject (стандартный claim)
        String userId = jwtAuth.getName();
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }

        // 2. Пробуем preferred_username (Keycloak)
        Object preferredUsername = jwtAuth.getTokenAttributes().get("preferred_username");
        if (preferredUsername != null) {
            return preferredUsername.toString();
        }

        // 3. Пробуем custom claim user_id
        Object customUserId = jwtAuth.getTokenAttributes().get("user_id");
        if (customUserId != null) {
            return customUserId.toString();
        }

        return null;
    }




    private String extractOrganizationId(JwtAuthenticationToken jwtAuth) {
        // 1. organization_id
        Object orgId = jwtAuth.getTokenAttributes().get("organization_id");
        if (orgId != null && !orgId.toString().isEmpty()) {
            return orgId.toString();
        }

        // 2. Из групп (первая группа = организация)
        Object groups = jwtAuth.getTokenAttributes().get("groups");
        if (groups instanceof List) {
            List<?> groupList = (List<?>) groups;
            if (!groupList.isEmpty()) {
                return groupList.get(0).toString();
            }
        }

        // 3. Fallback - userId (для обратной совместимости)
        return extractUserId(jwtAuth);
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
