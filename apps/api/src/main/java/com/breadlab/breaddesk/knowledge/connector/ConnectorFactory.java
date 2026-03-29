package com.breadlab.breaddesk.knowledge.connector;

import com.breadlab.breaddesk.knowledge.entity.KnowledgeConnectorEntity;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConnectorFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public KnowledgeConnector create(KnowledgeConnectorEntity entity) {
        Map<String, String> config = parseConfig(entity.getConfig());

        return switch (entity.getSourceType()) {
            case "confluence" -> new ConfluenceConnector(config);
            default -> throw new IllegalArgumentException("지원하지 않는 커넥터 타입: " + entity.getSourceType());
        };
    }

    private Map<String, String> parseConfig(String configJson) {
        try {
            return objectMapper.readValue(configJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("커넥터 설정 파싱 실패: {}", e.getMessage());
            return Map.of();
        }
    }
}
