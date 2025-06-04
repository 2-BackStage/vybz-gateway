package back.vybz.gateway_service.common.jwt.filter;

import back.vybz.gateway_service.common.jwt.AbstractJwtAuthenticationFilter;
import back.vybz.gateway_service.common.jwt.JwtProvider;
import org.springframework.stereotype.Component;

@Component
public class JwtUserAuthenticationFilter extends AbstractJwtAuthenticationFilter {

    public JwtUserAuthenticationFilter(JwtProvider jwtProvider) {
        super(jwtProvider);
    }

    @Override
    protected String extractUuid(String token) {
        return jwtProvider.getUserUuid(token);
    }

    @Override
    protected String getHeaderName() {
        return "X-User-Id";
    }
}
