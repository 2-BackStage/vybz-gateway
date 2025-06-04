package back.vybz.gateway_service.common.util;

import back.vybz.gateway_service.common.exception.BaseResponseStatus;
import back.vybz.gateway_service.common.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class JwtFilterUtils {
    public static boolean isTokenInvalid(String header) {
        return header == null || !header.startsWith("Bearer ");
    }

    public static Mono<Void> handleJwtError(ServerWebExchange exchange, BaseResponseStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<String> apiResponse = new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), status.getCode(), status.getMessage());

        byte[] data;
        try {
            data = new ObjectMapper().writeValueAsBytes(apiResponse);
        } catch (JsonProcessingException e) {
            data = new byte[0];
        }

        DataBuffer buffer = response.bufferFactory().wrap(data);
        return response.writeWith(Mono.just(buffer)).then();
    }
}
