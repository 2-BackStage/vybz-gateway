package back.vybz.gateway_service.common.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class JwtAuthFilterConfig {

    private List<String> skipPaths;
}
