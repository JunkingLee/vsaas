package com.vsaas.alarm.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
@RequiredArgsConstructor
public class AlarmWebSocketHandler extends TextWebSocketHandler {

    private final AlarmWsHub hub;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long tenantId = resolveTenantId(session);
        if (tenantId != null) {
            hub.register(tenantId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        hub.unregister(session);
    }

    private Long resolveTenantId(WebSocketSession session) {
        try {
            URI uri = session.getUri();
            if (uri == null) return null;
            String v = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("tenantId");
            return v != null ? Long.parseLong(v) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
