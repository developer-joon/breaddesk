package com.breadlab.breaddesk.channel.email;

import com.breadlab.breaddesk.channel.entity.ChannelConfig;
import com.breadlab.breaddesk.channel.entity.ChannelMessage;
import com.breadlab.breaddesk.channel.entity.ChannelType;
import com.breadlab.breaddesk.channel.repository.ChannelConfigRepository;
import com.breadlab.breaddesk.channel.repository.ChannelMessageRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.search.FlagTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Email receiver service using IMAP to poll inbox
 * Creates Inquiry per email message
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailReceiverService {

    private final ChannelConfigRepository channelConfigRepository;
    private final ChannelMessageRepository channelMessageRepository;
    private final ObjectMapper objectMapper;

    /**
     * Poll IMAP inbox every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void pollInbox() {
        log.debug("Polling email inboxes...");
        
        var emailConfigs = channelConfigRepository.findByChannelTypeAndIsActiveTrue("EMAIL");
        
        for (ChannelConfig config : emailConfigs) {
            try {
                pollSingleInbox(config);
            } catch (Exception e) {
                log.error("Failed to poll inbox for config {}: {}", config.getId(), e.getMessage(), e);
            }
        }
    }

    private void pollSingleInbox(ChannelConfig config) throws Exception {
        JsonNode credentials = objectMapper.readTree(config.getConfig());
        
        String host = credentials.path("imapHost").asText();
        int port = credentials.path("imapPort").asInt(993);
        String username = credentials.path("username").asText();
        String password = credentials.path("password").asText();
        boolean useSsl = credentials.path("useSsl").asBoolean(true);

        Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        props.put("mail.imap.host", host);
        props.put("mail.imap.port", port);
        props.put("mail.imap.ssl.enable", useSsl);
        props.put("mail.imap.starttls.enable", useSsl);

        Session session = Session.getInstance(props);
        Store store = null;
        Folder inbox = null;

        try {
            store = session.getStore("imaps");
            store.connect(host, username, password);
            
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Get unread messages
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            
            log.info("Found {} unread messages in {}", messages.length, config.getChannelType());

            for (Message message : messages) {
                try {
                    processIncomingEmail(message, config);
                    // Mark as read
                    message.setFlag(Flags.Flag.SEEN, true);
                } catch (Exception e) {
                    log.error("Failed to process email: {}", e.getMessage(), e);
                }
            }
        } finally {
            if (inbox != null && inbox.isOpen()) {
                inbox.close(false);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
    }

    private void processIncomingEmail(Message message, ChannelConfig config) throws Exception {
        String from = extractAddress(message.getFrom());
        String subject = message.getSubject();
        String content = getTextFromMessage(message);

        // Create sender info JSON
        var senderInfo = objectMapper.createObjectNode();
        senderInfo.put("email", from);
        senderInfo.put("name", extractName(message.getFrom()));

        // Create channel metadata for reply
        var metadata = objectMapper.createObjectNode();
        metadata.put("subject", subject);
        metadata.put("messageId", message.getHeader("Message-ID") != null ? 
                     message.getHeader("Message-ID")[0] : null);
        metadata.put("inReplyTo", message.getHeader("In-Reply-To") != null ? 
                     message.getHeader("In-Reply-To")[0] : null);

        ChannelMessage channelMessage = ChannelMessage.builder()
                .channelType(ChannelType.EMAIL)
                .source("email:" + config.getId())
                .content(subject + "\n\n" + content)
                .senderInfo(senderInfo.toString())
                .channelMetadata(metadata.toString())
                .createdAt(LocalDateTime.now())
                .processed(false)
                .build();

        channelMessageRepository.save(channelMessage);
        log.info("Saved email message from {} with subject: {}", from, subject);
    }

    private String extractAddress(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return "unknown@email.com";
        }
        InternetAddress addr = (InternetAddress) addresses[0];
        return addr.getAddress();
    }

    private String extractName(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return "Unknown";
        }
        InternetAddress addr = (InternetAddress) addresses[0];
        String personal = addr.getPersonal();
        return personal != null ? personal : addr.getAddress();
    }

    private String getTextFromMessage(Message message) throws Exception {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            result = getTextFromMultipart(multipart);
        }
        return result;
    }

    private String getTextFromMultipart(Multipart multipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent().toString());
            } else if (bodyPart.isMimeType("text/html")) {
                String html = bodyPart.getContent().toString();
                // Simple HTML strip (consider using Jsoup for production)
                result.append(html.replaceAll("<[^>]*>", ""));
            } else if (bodyPart.getContent() instanceof Multipart) {
                result.append(getTextFromMultipart((Multipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }
}
