package com.lecture.user.controller;

import com.lecture.user.dto.UserDto;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * BFF 토큰 교환 엔드포인트.
 * client_secret을 서버에만 두기 위해 인가 코드 → 토큰 교환을 서버가 대신 수행한다.
 * 토큰 발급 전 호출되므로 SecurityConfig에서 POST /api/users/token 은 permitAll 이다.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class AuthTokenController {

    private final RestTemplate restTemplate = new RestTemplate();

    private final String tokenUri;
    private final String clientId;
    private final String clientSecret;

    public AuthTokenController(
            @Value("${oauth.token-uri}") String tokenUri,
            @Value("${oauth.client-id}") String clientId,
            @Value("${oauth.client-secret}") String clientSecret) {
        this.tokenUri = tokenUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * POST /users/token - 인가 코드를 액세스 토큰으로 교환.
     * auth-server 응답(access_token/token_type/expires_in/refresh_token 등)을 그대로 data에 담아 돌려준다.
     */
    @PostMapping("/token")
    public ResponseEntity<UserDto.ApiResponse<Map<String, Object>>> exchangeToken(
            @Valid @RequestBody UserDto.TokenRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "authorization_code");
        form.add("code", request.getCode());
        form.add("redirect_uri", request.getRedirectUri());

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    tokenUri,
                    HttpMethod.POST,
                    new HttpEntity<>(form, headers),
                    new ParameterizedTypeReference<Map<String, Object>>() {});

            return ResponseEntity.ok(UserDto.ApiResponse.success(response.getBody()));

        } catch (HttpStatusCodeException e) {
            // auth-server가 코드/클라이언트 인증을 거부한 경우 — 원인 구분이 되도록 401은 401로 넘긴다
            HttpStatus status = e.getStatusCode().value() == HttpStatus.UNAUTHORIZED.value()
                    ? HttpStatus.UNAUTHORIZED
                    : HttpStatus.BAD_REQUEST;
            log.warn("[AuthTokenController] 토큰 교환 거부 - status: {}, body: {}",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            return ResponseEntity.status(status)
                    .body(UserDto.ApiResponse.error("토큰 교환에 실패했습니다: " + e.getResponseBodyAsString()));

        } catch (RestClientException e) {
            log.error("[AuthTokenController] 인증 서버 호출 실패 - tokenUri: {}, error: {}",
                    tokenUri, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UserDto.ApiResponse.error("인증 서버에 연결할 수 없습니다"));
        }
    }
}
