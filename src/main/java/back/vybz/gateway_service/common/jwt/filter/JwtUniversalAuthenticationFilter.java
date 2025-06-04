package back.vybz.gateway_service.common.jwt.filter;

import back.vybz.gateway_service.common.jwt.AbstractJwtAuthenticationFilter;
import back.vybz.gateway_service.common.jwt.JwtProvider;
import org.springframework.stereotype.Component;

@Component
public class JwtUniversalAuthenticationFilter extends AbstractJwtAuthenticationFilter {

    public JwtUniversalAuthenticationFilter(JwtProvider jwtProvider) {
        super(jwtProvider);
    }

    @Override
    protected String extractUuid(String token) {
        return jwtProvider.getUuidFromEither(token);
    }

    @Override
    protected String getHeaderName() {
        return "X-UUID";
    }
}
