package com.example.gateway;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtGrantedAuthoritiesConverterAdapter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;

@Slf4j
@Component
public class JwtAuthConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    static final String ACCESS_CLAIM = "realm_access";
    static final String ROLES_CLAIM = "roles";
    static final String ROLES_PREFIX = "ROLE_";

    @Override
    public Mono<AbstractAuthenticationToken> convert(final Jwt jwt) {
        val authorities = extractResourceRoles(jwt);
        return Mono.just(new JwtAuthenticationToken(jwt, authorities, jwt.getSubject()));
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(final Jwt jwt) {
        Object realmAccessObj = jwt.getClaim(ACCESS_CLAIM);
        if (realmAccessObj instanceof Map<?, ?> realmAccess && realmAccess.get(ROLES_CLAIM) instanceof List<?> rolesList) {

            log.info("Extracting roles from realm_access claim: {}", rolesList);
            return rolesList.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(ROLES_PREFIX::concat) // prefix required by Spring Security for roles.
                    .map(SimpleGrantedAuthority::new)
                    .collect(toUnmodifiableSet());
        }
        return Collections.emptySet();
    }
}