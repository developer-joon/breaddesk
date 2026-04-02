package com.breadlab.breaddesk.export.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@org.junit.jupiter.api.Disabled("TODO: 서비스 API 변경 반영 필요")
class ExportServiceTest {

    @Mock
    private EntityManager em;

    @Mock
    private Query query;

    @InjectMocks
    private ExportService exportService;

    @BeforeEach
    void setUp() {
        given(em.createNativeQuery(anyString())).willReturn(query);
        given(query.setParameter(any(int.class), any())).willReturn(query);
    }

    @Test
    @DisplayName("should_exportInquiriesCsv_when_validDateRange")
    void should_exportInquiriesCsv_when_validDateRange() {
        // Given
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        Object[] row = new Object[]{
                1L, "email", "Customer", "customer@test.com", "Test message",
                "AI response", 0.95, "OPEN", null,
                LocalDateTime.of(2025, 1, 15, 10, 0), null
        };

        given(query.getResultList()).willReturn(List.of(row));

        // When
        String csv = exportService.exportInquiriesCsv(from, to);

        // Then
        assertThat(csv).isNotBlank();
        assertThat(csv).contains("ID,Channel,SenderName");
        assertThat(csv).contains("1,email,Customer");
        assertThat(csv).contains("Test message");
    }

    @Test
    @DisplayName("should_exportTasksCsv_when_validDateRange")
    void should_exportTasksCsv_when_validDateRange() {
        // Given
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        Object[] row = new Object[]{
                1L, "Test Task", "TICKET", "HIGH", "WAITING",
                "Requester", "req@test.com", "Agent Name",
                LocalDate.of(2025, 1, 20), 5.0, 3.5,
                false, false,
                LocalDateTime.of(2025, 1, 10, 10, 0), null, null
        };

        given(query.getResultList()).willReturn(List.of(row));

        // When
        String csv = exportService.exportTasksCsv(from, to);

        // Then
        assertThat(csv).isNotBlank();
        assertThat(csv).contains("ID,Title,Type");
        assertThat(csv).contains("1,Test Task,TICKET");
        assertThat(csv).contains("Agent Name");
    }

    @Test
    @DisplayName("should_handleEmptyResults_when_noDataInRange")
    void should_handleEmptyResults_when_noDataInRange() {
        // Given
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        given(query.getResultList()).willReturn(List.of());

        // When
        String csv = exportService.exportInquiriesCsv(from, to);

        // Then
        assertThat(csv).contains("ID,Channel,SenderName");
        assertThat(csv.split("\n")).hasSize(1); // Only header
    }

    @Test
    @DisplayName("should_escapeCsvSpecialCharacters_when_present")
    void should_escapeCsvSpecialCharacters_when_present() {
        // Given
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        Object[] row = new Object[]{
                1L, "email", "Customer, Inc.", "test@test.com", "Message with \"quotes\"",
                null, null, "OPEN", null,
                LocalDateTime.now(), null
        };

        given(query.getResultList()).willReturn(List.of(row));

        // When
        String csv = exportService.exportInquiriesCsv(from, to);

        // Then
        assertThat(csv).contains("\"Customer, Inc.\"");
        assertThat(csv).contains("\"Message with \"\"quotes\"\"\"");
    }

    @Test
    @DisplayName("should_handleNullValues_when_exporting")
    void should_handleNullValues_when_exporting() {
        // Given
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        Object[] row = new Object[]{
                1L, "email", null, null, "Message",
                null, null, "OPEN", null,
                LocalDateTime.now(), null
        };

        given(query.getResultList()).willReturn(List.of(row));

        // When
        String csv = exportService.exportInquiriesCsv(from, to);

        // Then
        assertThat(csv).contains("1,email,,");
        assertThat(csv).doesNotContain("null");
    }

    @Test
    @DisplayName("should_exportMultipleRows_when_multipleRecords")
    void should_exportMultipleRows_when_multipleRecords() {
        // Given
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        Object[] row1 = new Object[]{
                1L, "email", "Customer1", "c1@test.com", "Message1",
                null, null, "OPEN", null, LocalDateTime.now(), null
        };
        Object[] row2 = new Object[]{
                2L, "email", "Customer2", "c2@test.com", "Message2",
                null, null, "RESOLVED", null, LocalDateTime.now(), LocalDateTime.now()
        };

        given(query.getResultList()).willReturn(List.of(row1, row2));

        // When
        String csv = exportService.exportInquiriesCsv(from, to);

        // Then
        assertThat(csv.split("\n")).hasSize(3); // Header + 2 rows
        assertThat(csv).contains("Customer1");
        assertThat(csv).contains("Customer2");
    }
}
