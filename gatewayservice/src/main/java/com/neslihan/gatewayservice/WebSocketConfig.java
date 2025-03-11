package com.neslihan.gatewayservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class WebSocketConfig implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (request.getURI().getPath().startsWith("/ws")) {
            return chain.filter(exchange.mutate()
                    .request(request.mutate()
                            .header("Connection", "Upgrade")
                            .header("Upgrade", "websocket")
                            .build())
                    .build());
        }
        return chain.filter(exchange);
    }
}

