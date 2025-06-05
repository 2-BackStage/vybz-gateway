package back.vybz.gateway_service.common.jwt.filter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class NoAuthFilter extends AbstractGatewayFilterFactory<Object> {

    public NoAuthFilter() {
        super(Object.class);
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            // 인증 처리 없이 그대로 통과
            return chain.filter(exchange);
        };
    }
}
