const BaseProvider = require('./BaseProvider');

/**
 * Google Gemini API Provider implementation
 * Supports non-streaming and streaming completions via Google AI Studio v1beta API.
 */
class GeminiProvider extends BaseProvider {
  constructor(config = {}) {
    super(config);
    this.baseUrl = config.baseUrl || 'https://generativelanguage.googleapis.com/v1beta';
    this.defaultModel = 'gemini-2.5-flash';
  }

  getName() {
    return 'gemini';
  }

  getDefaultModel() {
    return this.defaultModel;
  }

  getSupportedModels() {
    return [
      { id: 'gemini-2.5-flash', name: 'Gemini 2.5 Flash', contextWindow: 1048576, isDefault: true, tier: 'FREE_TIER_COMPATIBLE' },
      { id: 'gemini-3.5-flash', name: 'Gemini 3.5 Flash', contextWindow: 1048576, tier: 'FREE_TIER_COMPATIBLE' },
      { id: 'gemini-flash-latest', name: 'Gemini Flash Latest', contextWindow: 1048576, tier: 'FREE_TIER_COMPATIBLE' },
      { id: 'gemini-3.1-pro-preview', name: 'Gemini 3.1 Pro Preview', contextWindow: 2097152, tier: 'STANDARD' }
    ];
  }

  /**
   * Format generic messages into Gemini v1beta contents format
   */
  _formatContents(messages) {
    if (!Array.isArray(messages) || messages.length === 0) {
      return [{ role: 'user', parts: [{ text: 'Hello' }] }];
    }

    return messages.map(msg => ({
      role: msg.role === 'assistant' || msg.role === 'model' ? 'model' : 'user',
      parts: [{ text: typeof msg.content === 'string' ? msg.content : JSON.stringify(msg.content) }]
    }));
  }

  /**
   * Non-streaming content generation
   */
  async generate({
    messages,
    systemInstruction,
    model = this.defaultModel,
    temperature = 0.7,
    maxTokens = 4096,
    timeoutMs = 30000
  }) {
    if (!this.isAvailable()) {
      throw new Error('Gemini API key is not configured in server environment. Set GEMINI_API_KEY.');
    }

    const targetModel = model || this.defaultModel;
    const url = `${this.baseUrl}/models/${targetModel}:generateContent?key=${this.apiKey}`;

    const requestBody = {
      contents: this._formatContents(messages),
      generationConfig: {
        temperature,
        maxOutputTokens: maxTokens
      }
    };

    if (systemInstruction && systemInstruction.trim()) {
      requestBody.systemInstruction = {
        parts: [{ text: systemInstruction.trim() }]
      };
    }

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
        signal: controller.signal
      });

      clearTimeout(timer);

      const data = await response.json();

      if (!response.ok) {
        const errorMsg = data?.error?.message || `HTTP ${response.status}: Failed to generate from Gemini`;
        const err = new Error(errorMsg);
        err.status = response.status;
        err.code = data?.error?.status || 'GEMINI_ERROR';
        throw err;
      }

      const candidate = data.candidates?.[0];
      const text = candidate?.content?.parts?.map(p => p.text || '').join('') || '';
      const finishReason = candidate?.finishReason || 'STOP';

      const promptTokens = data.usageMetadata?.promptTokenCount || 0;
      const completionTokens = data.usageMetadata?.candidatesTokenCount || 0;
      const totalTokens = data.usageMetadata?.totalTokenCount || (promptTokens + completionTokens);

      return {
        text,
        model: targetModel,
        finishReason,
        usage: {
          promptTokens,
          completionTokens,
          totalTokens
        }
      };
    } catch (err) {
      clearTimeout(timer);
      if (err.name === 'AbortError') {
        const timeoutErr = new Error(`Gemini request timed out after ${timeoutMs}ms`);
        timeoutErr.status = 504;
        timeoutErr.code = 'TIMEOUT';
        throw timeoutErr;
      }
      throw err;
    }
  }

  /**
   * Server-Sent Events (SSE) streaming content generation
   */
  async stream({
    messages,
    systemInstruction,
    model = this.defaultModel,
    temperature = 0.7,
    maxTokens = 4096,
    timeoutMs = 60000,
    onChunk,
    onComplete,
    onError
  }) {
    if (!this.isAvailable()) {
      const err = new Error('Gemini API key is not configured in server environment. Set GEMINI_API_KEY.');
      err.status = 503;
      err.code = 'PROVIDER_NOT_CONFIGURED';
      if (onError) onError(err);
      throw err;
    }

    const targetModel = model || this.defaultModel;
    const url = `${this.baseUrl}/models/${targetModel}:streamGenerateContent?alt=sse&key=${this.apiKey}`;

    const requestBody = {
      contents: this._formatContents(messages),
      generationConfig: {
        temperature,
        maxOutputTokens: maxTokens
      }
    };

    if (systemInstruction && systemInstruction.trim()) {
      requestBody.systemInstruction = {
        parts: [{ text: systemInstruction.trim() }]
      };
    }

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody),
        signal: controller.signal
      });

      if (!response.ok) {
        clearTimeout(timer);
        const data = await response.json().catch(() => ({}));
        const errorMsg = data?.error?.message || `HTTP ${response.status}: Failed to stream from Gemini`;
        const err = new Error(errorMsg);
        err.status = response.status;
        err.code = data?.error?.status || 'GEMINI_ERROR';
        if (onError) onError(err);
        throw err;
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      let accumulatedText = '';
      let promptTokens = 0;
      let completionTokens = 0;

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          const trimmed = line.trim();
          if (trimmed.startsWith('data: ')) {
            const jsonStr = trimmed.slice(6).trim();
            if (jsonStr === '[DONE]') continue;

            try {
              const parsed = JSON.parse(jsonStr);
              const candidate = parsed.candidates?.[0];
              const chunkText = candidate?.content?.parts?.map(p => p.text || '').join('') || '';

              if (chunkText) {
                accumulatedText += chunkText;
                if (onChunk) onChunk(chunkText);
              }

              if (parsed.usageMetadata) {
                promptTokens = parsed.usageMetadata.promptTokenCount || promptTokens;
                completionTokens = parsed.usageMetadata.candidatesTokenCount || completionTokens;
              }
            } catch (parseErr) {
              // Ignore partial JSON chunks until full frame arrives
            }
          }
        }
      }

      clearTimeout(timer);

      const summary = {
        text: accumulatedText,
        model: targetModel,
        usage: {
          promptTokens,
          completionTokens,
          totalTokens: promptTokens + completionTokens
        }
      };

      if (onComplete) onComplete(summary);
      return summary;
    } catch (err) {
      clearTimeout(timer);
      if (err.name === 'AbortError') {
        const timeoutErr = new Error(`Gemini stream timed out after ${timeoutMs}ms`);
        timeoutErr.status = 504;
        timeoutErr.code = 'TIMEOUT';
        if (onError) onError(timeoutErr);
        throw timeoutErr;
      }
      if (onError) onError(err);
      throw err;
    }
  }
}

module.exports = GeminiProvider;
