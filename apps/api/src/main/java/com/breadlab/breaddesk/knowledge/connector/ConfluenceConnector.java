package com.breadlab.breaddesk.knowledge.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class ConfluenceConnector implements KnowledgeConnector {

    private final WebClient webClient;
    private final String spaceKey;
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConfluenceConnector(Map<String, String> config) {
        this.baseUrl = config.get("baseUrl");
        this.spaceKey = config.get("spaceKey");
        String token = config.get("token");

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl + "/wiki/api/v2")
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public String getSourceType() {
        return "confluence";
    }

    @Override
    public List<KnowledgeDocument> fetchDocuments() {
        return fetchPages(null);
    }

    @Override
    public List<KnowledgeDocument> fetchUpdatedSince(Instant lastSync) {
        return fetchPages(lastSync);
    }

    @Override
    public boolean testConnection() {
        try {
            webClient.get().uri("/spaces?limit=1").retrieve().bodyToMono(String.class).block();
            return true;
        } catch (Exception e) {
            log.error("Confluence 연결 테스트 실패: {}", e.getMessage());
            return false;
        }
    }

    private List<KnowledgeDocument> fetchPages(Instant since) {
        List<KnowledgeDocument> documents = new ArrayList<>();
        String cursor = null;

        try {
            do {
                String uri = "/spaces/" + spaceKey + "/pages?limit=25&body-format=storage";
                if (cursor != null) uri += "&cursor=" + cursor;

                String response = webClient.get().uri(uri).retrieve().bodyToMono(String.class).block();
                JsonNode root = objectMapper.readTree(response);
                JsonNode results = root.path("results");

                for (JsonNode page : results) {
                    String pageId = page.path("id").asText();
                    String title = page.path("title").asText();
                    String htmlContent = page.path("body").path("storage").path("value").asText("");
                    String lastModified = page.path("version").path("createdAt").asText("");

                    if (since != null && !lastModified.isEmpty()) {
                        Instant modified = OffsetDateTime.parse(lastModified).toInstant();
                        if (modified.isBefore(since)) continue;
                    }

                    String textContent = htmlToText(htmlContent);
                    if (textContent.isBlank()) continue;

                    String pageUrl = baseUrl + "/wiki/spaces/" + spaceKey + "/pages/" + pageId;

                    documents.add(new KnowledgeDocument(
                            pageId, "confluence", title, textContent, pageUrl,
                            List.of("confluence", spaceKey),
                            lastModified.isEmpty() ? Instant.now() : OffsetDateTime.parse(lastModified).toInstant()
                    ));
                }

                JsonNode links = root.path("_links");
                if (links.has("next")) {
                    String next = links.path("next").asText();
                    int cursorIdx = next.indexOf("cursor=");
                    cursor = cursorIdx >= 0 ? next.substring(cursorIdx + 7) : null;
                } else {
                    cursor = null;
                }
            } while (cursor != null);

        } catch (Exception e) {
            log.error("Confluence 페이지 가져오기 실패: {}", e.getMessage(), e);
        }

        log.info("Confluence에서 {} 문서 가져옴 (space: {})", documents.size(), spaceKey);
        return documents;
    }

    private String htmlToText(String html) {
        if (html == null || html.isBlank()) return "";
        return Jsoup.parse(html).text();
    }
}
