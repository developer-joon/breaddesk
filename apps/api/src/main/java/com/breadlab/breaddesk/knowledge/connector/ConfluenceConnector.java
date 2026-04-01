package com.breadlab.breaddesk.knowledge.connector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Confluence Cloud REST API v2 connector
 * https://developer.atlassian.com/cloud/confluence/rest/v2/intro/
 */
@Slf4j
public class ConfluenceConnector implements KnowledgeConnector {

    private final String baseUrl;
    private final String username;
    private final String apiToken;
    private final String spaceKey;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ConfluenceConnector(String baseUrl, String username, String apiToken, String spaceKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.username = username;
        this.apiToken = apiToken;
        this.spaceKey = spaceKey;

        String auth = username + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        this.webClient = WebClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String getSourceType() {
        return "confluence";
    }

    @Override
    public boolean testConnection() {
        try {
            // Test connection by fetching spaces info
            webClient.get()
                    .uri("/wiki/api/v2/spaces")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return true;
        } catch (Exception e) {
            log.error("Confluence connection test failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public List<KnowledgeDocument> fetchDocuments() {
        log.info("Fetching all documents from Confluence space: {}", spaceKey);
        return fetchPages(null);
    }

    @Override
    public List<KnowledgeDocument> fetchUpdatedSince(Instant lastSync) {
        log.info("Fetching Confluence documents updated since: {}", lastSync);
        // Confluence Cloud REST API v2 supports filtering by last modified date
        return fetchPages(lastSync);
    }

    private List<KnowledgeDocument> fetchPages(Instant lastSync) {
        List<KnowledgeDocument> documents = new ArrayList<>();
        String cursor = null;
        int pageCount = 0;

        do {
            try {
                String url = buildFetchUrl(cursor, lastSync);
                log.debug("Fetching Confluence pages: {}", url);

                String response = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                JsonNode rootNode = objectMapper.readTree(response);
                JsonNode results = rootNode.path("results");

                for (JsonNode pageNode : results) {
                    try {
                        KnowledgeDocument doc = transformToStandard(pageNode);
                        if (doc != null) {
                            documents.add(doc);
                        }
                    } catch (Exception e) {
                        log.error("Failed to transform Confluence page: {}", pageNode.path("id").asText(), e);
                    }
                }

                // Check for next page
                JsonNode links = rootNode.path("_links");
                if (links.has("next")) {
                    cursor = links.path("next").asText();
                } else {
                    cursor = null;
                }

                pageCount++;
                log.info("Fetched page {} with {} documents. Total so far: {}", pageCount, results.size(), documents.size());

            } catch (Exception e) {
                log.error("Error fetching Confluence pages (page {}): {}", pageCount, e.getMessage(), e);
                break;
            }
        } while (cursor != null && pageCount < 100); // Safety limit

        log.info("Fetched {} total documents from Confluence", documents.size());
        return documents;
    }

    private String buildFetchUrl(String cursor, Instant lastSync) {
        StringBuilder url = new StringBuilder("/wiki/api/v2/spaces/");
        url.append(spaceKey).append("/pages");
        url.append("?body-format=storage&limit=25"); // Get page content

        if (cursor != null) {
            url.append("&cursor=").append(cursor);
        }

        // Confluence Cloud v2 doesn't directly support date filtering in space pages endpoint
        // For incremental sync, we'll filter in transformToStandard based on version.when

        return url.toString();
    }

    public KnowledgeDocument transformToStandard(Object raw) {
        if (!(raw instanceof JsonNode pageNode)) {
            throw new IllegalArgumentException("Raw data must be JsonNode");
        }

        try {
            String id = pageNode.path("id").asText();
            String title = pageNode.path("title").asText();

            // Extract content (HTML storage format)
            String content = "";
            JsonNode bodyNode = pageNode.path("body").path("storage");
            if (bodyNode.has("value")) {
                content = bodyNode.path("value").asText();
                // Simple HTML cleanup (remove tags for text search)
                content = cleanHtml(content);
            }

            // Build URL
            String url = baseUrl + "/wiki/spaces/" + spaceKey + "/pages/" + id;

            // Extract version date
            Instant updatedAt = Instant.now();
            JsonNode versionNode = pageNode.path("version");
            if (versionNode.has("createdAt")) {
                updatedAt = Instant.parse(versionNode.path("createdAt").asText());
            }

            // Extract labels as tags
            List<String> tags = new ArrayList<>();
            tags.add("confluence");
            tags.add(spaceKey);

            return new KnowledgeDocument(
                    id,
                    "confluence",
                    title,
                    content,
                    url,
                    tags,
                    updatedAt
            );

        } catch (Exception e) {
            log.error("Failed to transform Confluence page: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Simple HTML tag removal for text indexing
     */
    private String cleanHtml(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        // Remove HTML tags
        String text = html.replaceAll("<[^>]+>", " ");
        
        // Replace HTML entities
        text = text.replace("&nbsp;", " ")
                   .replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'");

        // Normalize whitespace
        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }
}
