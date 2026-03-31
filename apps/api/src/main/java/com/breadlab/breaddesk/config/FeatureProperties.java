package com.breadlab.breaddesk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "breaddesk.features")
public class FeatureProperties {
    private boolean kanbanTasks = false;
    private boolean internalNotes = true;
    private boolean aiAssignment = false;
    private boolean jiraIntegration = false;
}
