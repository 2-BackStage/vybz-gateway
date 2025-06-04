package back.vybz.gateway_service.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
public class JwtAuthFilterConfig {

    private List<String> skipPaths;
}
