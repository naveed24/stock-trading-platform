package com.tradex.api_gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * User-facing API authorization for an isolated demonstration deployment.
 * Backend service ports must NOT be reachable from outside the Docker network.
 */
@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {
    private static final Set<String> WALLET_POSTS =
            Set.of("/api/wallet/create", "/api/wallet/deposit", "/api/wallet/withdraw");
    private final SecretKey signingKey;
    private final ObjectMapper json = new ObjectMapper();

    public JwtGatewayFilter(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32)
            throw new IllegalStateException("JWT_SECRET must contain at least 32 bytes");
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int getOrder() { return -100; }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        if (path.equals("/api/auth/login") || path.equals("/api/auth/register"))
            return method.equals("POST") ? chain.filter(exchange) : reject(exchange, HttpStatus.METHOD_NOT_ALLOWED);
        if (path.equals("/api/auth/status") && method.equals("GET"))
            return chain.filter(exchange);

        if (path.equals("/api/stocks") || path.startsWith("/api/stocks/")) {
            if (method.equals("GET")) return chain.filter(exchange);
            Claims admin = claims(exchange);
            if (admin == null) return reject(exchange, HttpStatus.UNAUTHORIZED);
            return "ADMIN".equals(admin.get("role"))
                    ? chain.filter(exchange) : reject(exchange, HttpStatus.FORBIDDEN);
        }

        Claims payload = claims(exchange);
        if (payload == null || !(payload.get("userId") instanceof Number))
            return reject(exchange, HttpStatus.UNAUTHORIZED);
        long owner = ((Number) payload.get("userId")).longValue();

        if (path.startsWith("/api/wallet/")) {
            if (method.equals("POST") && WALLET_POSTS.contains(path))
                return verifyBodyOwner(exchange, chain, owner);
            if (method.equals("GET")) {
                String id = path.startsWith("/api/wallet/transactions/")
                        ? path.substring("/api/wallet/transactions/".length())
                        : path.substring("/api/wallet/".length());
                return owns(id, owner) ? chain.filter(exchange) : reject(exchange, HttpStatus.FORBIDDEN);
            }
            return reject(exchange, HttpStatus.FORBIDDEN);
        }
        if (path.startsWith("/api/orders/")) {
            if (method.equals("POST") && (path.equals("/api/orders/buy") || path.equals("/api/orders/sell")))
                return verifyBodyOwner(exchange, chain, owner);
            if (method.equals("GET") && path.startsWith("/api/orders/user/")) {
                String id = path.substring("/api/orders/user/".length()).split("/", 2)[0];
                return owns(id, owner) ? chain.filter(exchange) : reject(exchange, HttpStatus.FORBIDDEN);
            }
            // Lookup by order ID alone cannot establish its owner.
            return reject(exchange, HttpStatus.FORBIDDEN);
        }
        if (path.startsWith("/api/portfolio/")) {
            if (!method.equals("GET")) return reject(exchange, HttpStatus.FORBIDDEN);
            String remaining = path.substring("/api/portfolio/".length());
            String id = remaining.startsWith("transactions/")
                    ? remaining.substring("transactions/".length())
                    : remaining.split("/", 2)[0];
            return owns(id, owner) ? chain.filter(exchange) : reject(exchange, HttpStatus.FORBIDDEN);
        }
        return reject(exchange, HttpStatus.FORBIDDEN);
    }

    private Claims claims(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        try {
            return Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(header.substring(7)).getPayload();
        } catch (Exception invalidOrExpiredToken) { return null; }
    }

    private boolean owns(String raw, long id) {
        try { return Long.parseLong(raw) == id; }
        catch (NumberFormatException ignored) { return false; }
    }

    private Mono<Void> verifyBodyOwner(ServerWebExchange exchange, GatewayFilterChain chain, long userId) {
        return DataBufferUtils.join(exchange.getRequest().getBody()).flatMap(buffer -> {
            byte[] data = new byte[buffer.readableByteCount()];
            try { buffer.read(data); } finally { DataBufferUtils.release(buffer); }
            try {
                JsonNode node = json.readTree(data);
                if (node == null || !node.has("userId") || !node.get("userId").isIntegralNumber()
                        || node.get("userId").longValue() != userId)
                    return reject(exchange, HttpStatus.FORBIDDEN);
            } catch (Exception malformed) {
                return reject(exchange, HttpStatus.BAD_REQUEST);
            }
            ServerHttpRequest decorated = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override public Flux<DataBuffer> getBody() {
                    return Flux.defer(() -> Flux.just(exchange.getResponse().bufferFactory().wrap(data)));
                }
            };
            return chain.filter(exchange.mutate().request(decorated).build());
        }).switchIfEmpty(Mono.defer(() -> reject(exchange, HttpStatus.BAD_REQUEST)));
    }

    private Mono<Void> reject(ServerWebExchange exchange, HttpStatus status) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] result = ("{\"message\":\"Access denied (" + status.value() + ")\"}")
                .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(result)));
    }
}
