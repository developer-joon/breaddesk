package com.breadlab.breaddesk.knowledge.connector;

import java.time.Instant;
import java.util.List;

/**
 * 지식 소스 커넥터 인터페이스
 * 새 소스 추가 시 이 인터페이스만 구현하면 됨
 */
public interface KnowledgeConnector {

    /**
     * 소스 타입 (confluence, datadog, argocd 등)
     */
    String getSourceType();

    /**
     * 전체 문서 가져오기
     */
    List<KnowledgeDocument> fetchDocuments();

    /**
     * 특정 시점 이후 변경된 문서만 가져오기 (증분 동기화)
     */
    List<KnowledgeDocument> fetchUpdatedSince(Instant lastSync);

    /**
     * 연결 테스트
     */
    boolean testConnection();
}
