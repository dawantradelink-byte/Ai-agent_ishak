/**
 * SecretRedactor Service
 * Ensures API keys, bot tokens, and credentials are never logged or leaked.
 */
class SecretRedactor {
  constructor() {
    this.patterns = [
      // Telegram bot token: 123456789:ABCdefGhIJKlmNoPQRsTUVwxyZ_1234567
      /\b\d{8,11}:[A-Za-z0-9_-]{34,38}\b/g,
      // Google API Key: AIzaSy...
      /\bAIzaSy[A-Za-z0-9_-]{33}\b/g,
      // Generic Bearer / API keys
      /Bearer\s+[A-Za-z0-9_\-\.]{16,}/gi,
      /key=[A-Za-z0-9_\-]{20,}/gi,
      /x-api-key:\s*[A-Za-z0-9_\-]{20,}/gi
    ];
  }

  redact(text) {
    if (typeof text !== 'string') return text;
    let redacted = text;

    for (const pattern of this.patterns) {
      redacted = redacted.replace(pattern, '[REDACTED_SECRET]');
    }

    // Redact explicitly known environment secrets if present
    const envSecrets = [
      process.env.GEMINI_API_KEY,
      process.env.OPENAI_API_KEY,
      process.env.TELEGRAM_BOT_TOKEN,
      process.env.BACKEND_AUTH_SECRET
    ].filter(s => s && s.length > 8);

    for (const secret of envSecrets) {
      redacted = redacted.split(secret).join('[REDACTED_SECRET]');
    }

    return redacted;
  }
}

module.exports = new SecretRedactor();
