package com.lecture.course.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Collection;

@Configuration
@EnableWebSecurity

public class SecurityConfig {

    /**
     * 기본 JwtAuthenticationConverter는 scope 클레임만 SCOPE_x 권한으로 변환한다.
     * auth-server가 발급하는 토큰의 role 클레임(STUDENT/INSTRUCTOR)을 ROLE_x 권한으로 추가 매핑해야
     * hasAuthority("ROLE_INSTRUCTOR") 체크가 실제로 동작한다.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>(scopeConverter.convert(jwt));
            String role = jwt.getClaimAsString("role");
            if (role != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.addAllowedOriginPattern("*");
                config.addAllowedMethod("*");
                config.addAllowedHeader("*");
                return config;
            }))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // 내부 서비스 호출 (enrollment/recommend-service → course-service)
                // TODO: 서비스 간 client-credentials 토큰 전파가 아직 구현되어 있지 않아 permitAll 유지.
                // WebClient(enrollment-service)/course_client.py(recommend-service) 모두 Authorization 헤더를 붙이지 않음.
                // SCOPE_service.read로 막으면 계약 신청/결제 확정/추천 흐름이 전부 401로 깨짐.
                .requestMatchers("/api/courses/internal/**").permitAll()
                // 강의 등록은 INSTRUCTOR만
                .requestMatchers(HttpMethod.POST, "/api/courses").hasAuthority("ROLE_INSTRUCTOR")
                // 강의 목록/상세 조회는 인증 불필요
                .requestMatchers(HttpMethod.GET, "/api/courses", "/api/courses/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );
        return http.build();
    }
}