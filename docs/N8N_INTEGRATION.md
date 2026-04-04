# n8n Integration Guide

BreadDesk provides a simplified webhook API for easy integration with n8n and other automation tools.

## Simple Webhook API

### Endpoint

```
POST /api/v1/webhooks/simple
```

### Authentication

Add the following header to your request:

```
X-Webhook-Token: your-token-here
```

**Setting up the token:**

1. Edit your `application.yml` or set environment variable:
   ```yaml
   breaddesk:
     webhook:
       default-token: your-secure-random-token
   ```

2. Or use environment variable:
   ```bash
   export WEBHOOK_DEFAULT_TOKEN=your-secure-random-token
   ```

### Request Format

```json
{
  "channel": "email",
  "senderName": "김철수",
  "senderEmail": "cheolsu@example.com",
  "message": "문의 내용입니다",
  "metadata": {
    "source": "contact-form",
    "referrer": "homepage"
  }
}
```

**Required fields:**
- `channel` - Channel type: `email`, `kakao`, `slack`, `telegram`, `custom`
- `senderName` - Name of the person making the inquiry
- `message` - The inquiry message content

**Optional fields:**
- `senderEmail` - Email address of the sender
- `metadata` - Additional key-value data (stored as channelMeta)

### Response Format

**Success:**
```json
{
  "success": true,
  "data": {
    "inquiryId": 123,
    "status": "AI_ANSWERED",
    "aiResponse": "자동 답변 내용...",
    "aiConfidence": 0.85
  }
}
```

**Error:**
```json
{
  "success": false,
  "error": "Error message"
}
```

## n8n Setup Guide

### Step 1: Create HTTP Request Node

1. Add an **HTTP Request** node to your workflow
2. Configure the node:

**Authentication:**
- Method: `POST`
- URL: `https://your-breaddesk-url.com/api/v1/webhooks/simple`
- Authentication: `Header Auth`
  - Name: `X-Webhook-Token`
  - Value: `your-token-here`

**Body:**
```json
{
  "channel": "{{ $json.channel }}",
  "senderName": "{{ $json.senderName }}",
  "senderEmail": "{{ $json.senderEmail }}",
  "message": "{{ $json.message }}",
  "metadata": {
    "source": "n8n",
    "workflowId": "{{ $workflow.id }}"
  }
}
```

### Step 2: Example Workflows

#### Email to Inquiry

```
[Email Trigger] → [HTTP Request to BreadDesk]
```

1. **Email Trigger** node:
   - Configure your email account
   - Filter: Only unread emails

2. **HTTP Request** node:
   ```json
   {
     "channel": "email",
     "senderName": "{{ $json.from.name }}",
     "senderEmail": "{{ $json.from.email }}",
     "message": "{{ $json.text }}"
   }
   ```

#### Slack Message to Inquiry

```
[Slack Trigger] → [HTTP Request to BreadDesk]
```

1. **Slack Trigger** node:
   - Event: New Message in Channel
   - Channel: #support

2. **HTTP Request** node:
   ```json
   {
     "channel": "slack",
     "senderName": "{{ $json.user.name }}",
     "message": "{{ $json.text }}",
     "metadata": {
       "slackUserId": "{{ $json.user.id }}",
       "channelId": "{{ $json.channel }}"
     }
   }
   ```

#### Contact Form to Inquiry

```
[Webhook] → [Set Variables] → [HTTP Request to BreadDesk]
```

1. **Webhook** node:
   - Listen to POST requests from your website form

2. **Set** node (optional):
   - Clean/validate form data

3. **HTTP Request** node:
   ```json
   {
     "channel": "custom",
     "senderName": "{{ $json.name }}",
     "senderEmail": "{{ $json.email }}",
     "message": "{{ $json.message }}",
     "metadata": {
       "formId": "contact-us",
       "pageUrl": "{{ $json.referrer }}"
     }
   }
   ```

### Step 3: Error Handling

Add an **IF** node after the HTTP Request:

```
Condition: {{ $json.success }} = true

✅ Success Branch:
   → Send confirmation email/notification

❌ Error Branch:
   → Log to error tracking
   → Send admin alert
```

## Testing

### Using curl

```bash
curl -X POST https://your-breaddesk-url.com/api/v1/webhooks/simple \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Token: your-token-here" \
  -d '{
    "channel": "custom",
    "senderName": "Test User",
    "senderEmail": "test@example.com",
    "message": "This is a test inquiry"
  }'
```

### Expected Response

```json
{
  "success": true,
  "data": {
    "inquiryId": 1,
    "status": "AI_ANSWERED",
    "aiResponse": "AI가 생성한 답변...",
    "aiConfidence": 0.75
  }
}
```

## Supported Channels

- `email` - Email inquiries
- `kakao` - KakaoTalk messages
- `slack` - Slack messages
- `telegram` - Telegram messages
- `custom` - Generic/other sources

## Security Notes

1. **Keep your webhook token secret** - Never commit it to version control
2. **Use HTTPS** - Always use secure connections in production
3. **Validate in n8n** - Add validation nodes before sending to BreadDesk
4. **Monitor usage** - Check BreadDesk logs for suspicious activity
5. **Rotate tokens** - Change webhook token periodically

## Troubleshooting

### 401 Unauthorized

- Check if X-Webhook-Token header is set correctly
- Verify token matches the one in application.yml

### 400 Bad Request

- Ensure required fields (channel, senderName, message) are present
- Check JSON format is valid

### 500 Internal Server Error

- Check BreadDesk API logs
- Verify database connection
- Check if AI service is running

## Advanced Usage

### Custom Metadata

Use the `metadata` field to store custom data:

```json
{
  "metadata": {
    "priority": "high",
    "department": "sales",
    "campaign": "spring-2024",
    "customField1": "value1"
  }
}
```

### Multi-channel Routing

Use n8n's **Switch** node to route different sources:

```
[Trigger] → [Switch by Source] → [HTTP Request per Channel]
```

This allows customizing the `channel` field based on the source.

## Support

For issues or questions:
- Check BreadDesk logs: `kubectl logs -f deployment/breaddesk-api -n openclaw`
- Review n8n execution logs
- Verify network connectivity between n8n and BreadDesk

---

**Last updated:** 2026-04-04
