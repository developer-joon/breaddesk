package com.breadlab.breaddesk.channel.email;

import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * Email sender service using SMTP
 * Sends replies to customers via email
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final ChannelConfigRepository channelConfigRepository;
    private final ObjectMapper objectMapper;

    /**
     * Send email reply
     * 
     * @param configId Channel config ID
     * @param to Recipient email
     * @param toName Recipient name
     * @param subject Email subject
     * @param body Email body (HTML or plain text)
     * @param inReplyTo In-Reply-To header for threading
     */
    public void sendReply(Long configId, String to, String toName, String subject, 
                         String body, String inReplyTo) throws Exception {
        ChannelConfig config = channelConfigRepository.findById(configId)
                .orElseThrow(() -> new IllegalArgumentException("Channel config not found: " + configId));

        JsonNode credentials = objectMapper.readTree(config.getConfig());
        
        String smtpHost = credentials.path("smtpHost").asText();
        int smtpPort = credentials.path("smtpPort").asInt(587);
        String username = credentials.path("username").asText();
        String password = credentials.path("password").asText();
        String fromAddress = credentials.path("fromAddress").asText(username);
        String fromName = credentials.path("fromName").asText("BreadDesk Support");
        boolean useTls = credentials.path("useTls").asBoolean(true);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", useTls);
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress, fromName));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to, toName));
            message.setSubject(subject);

            // Set content type based on body
            if (body.contains("<html") || body.contains("<p>")) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body, "utf-8");
            }

            // Add In-Reply-To header for email threading
            if (inReplyTo != null && !inReplyTo.isEmpty()) {
                message.setHeader("In-Reply-To", inReplyTo);
                message.setHeader("References", inReplyTo);
            }

            Transport.send(message);
            log.info("Email sent to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Send simple reply without threading
     */
    public void sendReply(Long configId, String to, String toName, String subject, String body) 
            throws Exception {
        sendReply(configId, to, toName, subject, body, null);
    }
}
