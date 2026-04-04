package com.breadlab.breaddesk.automation.service;

import com.breadlab.breaddesk.automation.dto.AutomationRuleRequest;
import com.breadlab.breaddesk.automation.dto.AutomationRuleResponse;
import com.breadlab.breaddesk.automation.entity.AutomationRule;
import com.breadlab.breaddesk.automation.entity.TriggerType;
import com.breadlab.breaddesk.automation.repository.AutomationRuleRepository;
import com.breadlab.breaddesk.common.exception.ResourceNotFoundException;
import com.breadlab.breaddesk.inquiry.entity.Inquiry;
import com.breadlab.breaddesk.inquiry.entity.InquiryStatus;
import com.breadlab.breaddesk.inquiry.repository.InquiryRepository;
import com.breadlab.breaddesk.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AutomationRuleService {

    private final AutomationRuleRepository automationRuleRepository;
    private final InquiryRepository inquiryRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    public List<AutomationRuleResponse> getAllRules() {
        return automationRuleRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AutomationRuleResponse getRuleById(Long id) {
        AutomationRule rule = findRuleOrThrow(id);
        return toResponse(rule);
    }

    @Transactional
    public AutomationRuleResponse createRule(AutomationRuleRequest request) {
        AutomationRule rule = AutomationRule.builder()
                .name(request.getName())
                .active(request.getActive() != null ? request.getActive() : true)
                .triggerType(request.getTriggerType())
                .conditionJson(request.getConditionJson())
                .actionJson(request.getActionJson())
                .build();
        
        AutomationRule saved = automationRuleRepository.save(rule);
        log.info("Created automation rule: {} (ID: {})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public AutomationRuleResponse updateRule(Long id, AutomationRuleRequest request) {
        AutomationRule rule = findRuleOrThrow(id);
        
        if (request.getName() != null) rule.setName(request.getName());
        if (request.getActive() != null) rule.setActive(request.getActive());
        if (request.getTriggerType() != null) rule.setTriggerType(request.getTriggerType());
        if (request.getConditionJson() != null) rule.setConditionJson(request.getConditionJson());
        if (request.getActionJson() != null) rule.setActionJson(request.getActionJson());
        
        AutomationRule saved = automationRuleRepository.save(rule);
        log.info("Updated automation rule: {} (ID: {})", saved.getName(), saved.getId());
        return toResponse(saved);
    }

    @Transactional
    public void deleteRule(Long id) {
        AutomationRule rule = findRuleOrThrow(id);
        automationRuleRepository.delete(rule);
        log.info("Deleted automation rule: {} (ID: {})", rule.getName(), id);
    }

    @Transactional
    public void toggleRule(Long id, Boolean active) {
        AutomationRule rule = findRuleOrThrow(id);
        rule.setActive(active);
        automationRuleRepository.save(rule);
        log.info("Toggled automation rule {} to {}", id, active ? "ACTIVE" : "INACTIVE");
    }

    /**
     * Execute automation rules for an inquiry
     */
    @Transactional
    public void executeRulesForInquiry(Inquiry inquiry, TriggerType triggerType) {
        List<AutomationRule> rules = automationRuleRepository
                .findByActiveAndTriggerType(true, triggerType);
        
        log.debug("Executing {} active rules for trigger: {}, inquiry: {}", 
                rules.size(), triggerType, inquiry.getId());
        
        for (AutomationRule rule : rules) {
            try {
                if (evaluateCondition(inquiry, rule.getConditionJson())) {
                    executeAction(inquiry, rule.getActionJson());
                    log.info("Applied automation rule '{}' to inquiry #{}", 
                            rule.getName(), inquiry.getId());
                }
            } catch (Exception e) {
                log.error("Failed to execute automation rule {} for inquiry {}: {}", 
                        rule.getId(), inquiry.getId(), e.getMessage(), e);
            }
        }
    }

    private boolean evaluateCondition(Inquiry inquiry, String conditionJson) {
        try {
            JsonNode condition = objectMapper.readTree(conditionJson);
            String field = condition.get("field").asText();
            String operator = condition.get("operator").asText();
            String value = condition.get("value").asText();

            String fieldValue = getFieldValue(inquiry, field);
            if (fieldValue == null) return false;

            return switch (operator) {
                case "equals" -> fieldValue.equalsIgnoreCase(value);
                case "contains" -> fieldValue.toLowerCase().contains(value.toLowerCase());
                case "startsWith" -> fieldValue.toLowerCase().startsWith(value.toLowerCase());
                case "endsWith" -> fieldValue.toLowerCase().endsWith(value.toLowerCase());
                default -> {
                    log.warn("Unknown operator: {}", operator);
                    yield false;
                }
            };
        } catch (Exception e) {
            log.error("Failed to evaluate condition: {}", conditionJson, e);
            return false;
        }
    }

    private String getFieldValue(Inquiry inquiry, String field) {
        return switch (field) {
            case "channel" -> inquiry.getChannel();
            case "message" -> inquiry.getMessage();
            case "senderName" -> inquiry.getSenderName();
            case "senderEmail" -> inquiry.getSenderEmail();
            case "status" -> inquiry.getStatus().name();
            default -> {
                log.warn("Unknown field: {}", field);
                yield null;
            }
        };
    }

    private void executeAction(Inquiry inquiry, String actionJson) {
        try {
            JsonNode action = objectMapper.readTree(actionJson);
            String type = action.get("type").asText();

            switch (type) {
                case "SET_STATUS" -> {
                    String statusValue = action.get("value").asText();
                    InquiryStatus newStatus = InquiryStatus.valueOf(statusValue);
                    inquiry.setStatus(newStatus);
                    inquiryRepository.save(inquiry);
                    log.info("Set inquiry #{} status to {}", inquiry.getId(), newStatus);
                }
                case "ASSIGN" -> {
                    Long memberId = action.get("memberId").asLong();
                    // Note: Inquiry doesn't have assignee field, but could be added
                    // For now, just log the action
                    log.info("Would assign inquiry #{} to member {}", inquiry.getId(), memberId);
                }
                default -> log.warn("Unknown action type: {}", type);
            }
        } catch (Exception e) {
            log.error("Failed to execute action: {}", actionJson, e);
        }
    }

    private AutomationRule findRuleOrThrow(Long id) {
        return automationRuleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found: " + id));
    }

    private AutomationRuleResponse toResponse(AutomationRule rule) {
        return AutomationRuleResponse.builder()
                .id(rule.getId())
                .name(rule.getName())
                .active(rule.getActive())
                .triggerType(rule.getTriggerType())
                .conditionJson(rule.getConditionJson())
                .actionJson(rule.getActionJson())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
