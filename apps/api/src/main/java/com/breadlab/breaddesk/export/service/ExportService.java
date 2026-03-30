package com.breadlab.breaddesk.export.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportService {

    private final EntityManager em;

    public String exportInquiriesCsv(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        """
                        SELECT i.id, i.channel, i.sender_name, i.sender_email, i.message,
                               i.ai_response, i.ai_confidence, i.status, i.resolved_by,
                               i.created_at, i.resolved_at
                        FROM inquiries i
                        WHERE i.created_at BETWEEN ?1 AND ?2
                        ORDER BY i.created_at DESC
                        """)
                .setParameter(1, start).setParameter(2, end)
                .getResultList();

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Channel,SenderName,SenderEmail,Message,AIResponse,AIConfidence,Status,ResolvedBy,CreatedAt,ResolvedAt\n");
        for (Object[] row : rows) {
            sb.append(csvRow(row));
        }
        return sb.toString();
    }

    public String exportTasksCsv(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                        """
                        SELECT t.id, t.title, t.type, t.urgency, t.status,
                               t.requester_name, t.requester_email, m.name AS assignee_name,
                               t.due_date, t.estimated_hours, t.actual_hours,
                               t.sla_response_breached, t.sla_resolve_breached,
                               t.created_at, t.started_at, t.completed_at
                        FROM tasks t
                        LEFT JOIN members m ON m.id = t.assignee_id
                        WHERE t.created_at BETWEEN ?1 AND ?2
                        ORDER BY t.created_at DESC
                        """)
                .setParameter(1, start).setParameter(2, end)
                .getResultList();

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,Type,Urgency,Status,RequesterName,RequesterEmail,AssigneeName,DueDate,EstimatedHours,ActualHours,SLAResponseBreached,SLAResolveBreached,CreatedAt,StartedAt,CompletedAt\n");
        for (Object[] row : rows) {
            sb.append(csvRow(row));
        }
        return sb.toString();
    }

    private String csvRow(Object[] row) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < row.length; i++) {
            if (i > 0) sb.append(',');
            if (row[i] == null) {
                sb.append("");
            } else {
                String val = row[i].toString();
                if (val.contains(",") || val.contains("\"") || val.contains("\n")) {
                    sb.append('"').append(val.replace("\"", "\"\"")).append('"');
                } else {
                    sb.append(val);
                }
            }
        }
        sb.append('\n');
        return sb.toString();
    }
}
