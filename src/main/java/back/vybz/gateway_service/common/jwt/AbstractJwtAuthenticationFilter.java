package back.vybz.gateway_service.common.jwt;

import back.vybz.gateway_service.common.config.JwtAuthFilterConfig;
import back.vybz.gateway_service.common.exception.BaseResponseStatus;
import back.vybz.gateway_service.common.util.JwtFilterUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Slf4j
public abstract class AbstractJwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthFilterConfig> {

    protected final JwtProvider jwtProvider;

    public AbstractJwtAuthenticationFilter(JwtProvider jwtProvider) {
        super(JwtAuthFilterConfig.class);
        this.jwtProvider = jwtProvider;
    }

    @Override
    public GatewayFilter apply(JwtAuthFilterConfig config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            System.out.println("1");
            String authorizationHeader = request.getHeaders().getFirst("Authorization");
            String path = request.getURI().getPath();

            if (config.getSkipPaths() != null &&
                    config.getSkipPaths().stream().anyMatch(path::startsWith)) {
                log.info("[SKIP] 인증 제외 경로: {}", path);
                return chain.filter(exchange);
            }

            if (JwtFilterUtils.isTokenInvalid(authorizationHeader)) {
                return JwtFilterUtils.handleJwtError(exchange, BaseResponseStatus.NO_JWT_TOKEN);
            }

            System.out.println(authorizationHeader);

            String token = authorizationHeader.replace("Bearer ", "");
            if (!jwtProvider.validateToken(token)) {
                return JwtFilterUtils.handleJwtError(exchange, BaseResponseStatus.TOKEN_NOT_VALID);
            }

            String uuid = extractUuid(token);

            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(getHeaderName(),  uuid)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }

    protected abstract String extractUuid(String token);

    protected abstract String getHeaderName();
}