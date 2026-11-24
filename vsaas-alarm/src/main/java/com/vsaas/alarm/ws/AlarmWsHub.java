package com.vsaas.alarm.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vsaas.common.mq.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AlarmWsHub {

    private final Map<Long, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void register(Long tenantId, WebSocketSession session) {
        sessions.computeIfAbsent(tenantId, k -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("WS 接入 tenant={} session={}", tenantId, session.getId());
    }

    public void unregister(WebSocketSession session) {
        sessions.values().forEach(set -> set.remove(session));
    }

    public void pushToTenant(Long tenantId, DomainEvent event) {
        Set<WebSocketSession> set = sessions.get(tenantId);
        if (set == null || set.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            TextMessage msg = new TextMessage(json);
            for (WebSocketSession s : set) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(msg);
                    } catch (Exception e) {
                        log.warn("WS 推送失败 session={}: {}", s.getId(), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("WS 序列化失败: {}", e.getMessage());
        }
    }
}
