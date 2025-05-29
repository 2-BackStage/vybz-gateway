package back.vybz.gateway_service.common.jwt;

import back.vybz.gateway_service.common.exception.BaseResponseStatus;
import back.vybz.gateway_service.common.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        super(Config.class);
        this.jwtProvider = jwtProvider;
    }

    public static class Config {
        private List<String> skipPaths;

        public List<String> getSkipPaths() {
            return skipPaths;
        }

        public void setSkipPaths(List<String> skipPaths) {
            this.skipPaths = skipPaths;
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String authorizationHeader = request.getHeaders().getFirst("Authorization");

            String path = request.getURI().getPath();

            // ✅ 인증 예외 경로 처리 (필터 스킵)
            if (config.skipPaths != null &&
                    config.skipPaths.stream().anyMatch(path::startsWith)) {
                log.info("[SKIP] 인증 제외 경로: {}", path);
                return chain.filter(exchange);
            }

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return handleException(exchange, BaseResponseStatus.NO_JWT_TOKEN);
            }

            String token = authorizationHeader.replace("Bearer ", "");
            if (!jwtProvider.validateToken(token)) {
                return handleException(exchange, BaseResponseStatus.TOKEN_NOT_VALID);
            }

            // ✅ 사용자 정보 추출 후 헤더로 전달
            String uuid = jwtProvider.getUserUuid(token);
            String role = jwtProvider.getUserRole(token);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", uuid)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    private Mono<Void> handleException(ServerWebExchange exchange, BaseResponseStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<String> apiResponse = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), status.getCode(), status.getMessage());

        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data;
        try {
            data = objectMapper.writeValueAsBytes(apiResponse);
        } catch (JsonProcessingException e) {
            data = new byte[0];
        }

        DataBuffer buffer = response.bufferFactory().wrap(data);
        return response.writeWith(Mono.just(buffer)).then(Mono.empty());
    }
}
