package back.vybz.gateway_service.common.jwt.filter;

import back.vybz.gateway_service.common.jwt.AbstractJwtAuthenticationFilter;
import back.vybz.gateway_service.common.jwt.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtBuskerAuthenticationFilter extends AbstractJwtAuthenticationFilter {

    public JwtBuskerAuthenticationFilter(JwtProvider jwtProvider) {
        super(jwtProvider);
    }

    @Override
    protected String extractUuid(String token) {
        return jwtProvider.getBuskerUuid(token);
    }

    @Override
    protected String getHeaderName() {
        return "X-Busker-Id";
    }
}
