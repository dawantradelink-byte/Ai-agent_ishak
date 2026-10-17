const BaseProvider = require('./BaseProvider');

/**
 * OpenAI / Compatible Provider implementation
 * Supports OpenAI chat completions and streaming via /v1/chat/completions.
 */
class OpenAIProvider extends BaseProvider {
  constructor(config = {}) {
    super(config);
    this.baseUrl = config.baseUrl || 'https://api.openai.com/v1';
    this.defaultModel = 'gpt-4o-mini';
  }

  getName() {
    return 'openai';
  }

  getDefaultModel() {
    return this.defaultModel;
  }

  getSupportedModels() {
    return [
      { id: 'gpt-4o-mini', name: 'GPT-4o Mini', contextWindow: 128000, isDefault: true, tier: 'TIER_1' },
      { id: 'gpt-4o', name: 'GPT-4o', contextWindow: 128000, tier: 'STANDARD' }
    ];
  }

  async generate({
    messages,
    systemInstruction,
    model = this.defaultModel,
    temperature = 0.7,
    maxTokens = 4096,
    timeoutMs = 30000
  }) {
    if (!this.isAvailable()) {
      throw new Error('OpenAI API key is not configured in server environment. Set OPENAI_API_KEY.');
    }

    const targetModel = model || this.defaultModel;
    const url = `${this.baseUrl}/chat/completions`;

    const formattedMessages = [];
    if (systemInstruction && systemInstruction.trim()) {
      formattedMessages.push({ role: 'system', content: systemInstruction.trim() });
    }
    for (const msg of (messages || [])) {
      formattedMessages.push({ role: msg.role === 'model' ? 'assistant' : msg.role, content: msg.content });
    }

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.apiKey}`
        },
        body: JSON.stringify({
          model: targetModel,
          messages: formattedMessages,
          temperature,
          max_tokens: maxTokens
        }),
        signal: controller.signal
      });

      clearTimeout(timer);
      const data = await response.json();

      if (!response.ok) {
        const errorMsg = data?.error?.message || `HTTP ${response.status}: Failed to generate from OpenAI`;
        const err = new Error(errorMsg);
        err.status = response.status;
        err.code = data?.error?.code || 'OPENAI_ERROR';
        throw err;
      }

      const choice = data.choices?.[0];
      const text = choice?.message?.content || '';
      const finishReason = choice?.finish_reason || 'stop';

      return {
        text,
        model: targetModel,
        finishReason,
        usage: {
          promptTokens: data.usage?.prompt_tokens || 0,
          completionTokens: data.usage?.completion_tokens || 0,
          totalTokens: data.usage?.total_tokens || 0
        }
      };
    } catch (err) {
      clearTimeout(timer);
      if (err.name === 'AbortError') {
        const timeoutErr = new Error(`OpenAI request timed out after ${timeoutMs}ms`);
        timeoutErr.status = 504;
        timeoutErr.code = 'TIMEOUT';
        throw timeoutErr;
      }
      throw err;
    }
  }

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
      const err = new Error('OpenAI API key is not configured in server environment. Set OPENAI_API_KEY.');
      err.status = 503;
      err.code = 'PROVIDER_NOT_CONFIGURED';
      if (onError) onError(err);
      throw err;
    }

    const targetModel = model || this.defaultModel;
    const url = `${this.baseUrl}/chat/completions`;

    const formattedMessages = [];
    if (systemInstruction && systemInstruction.trim()) {
      formattedMessages.push({ role: 'system', content: systemInstruction.trim() });
    }
    for (const msg of (messages || [])) {
      formattedMessages.push({ role: msg.role === 'model' ? 'assistant' : msg.role, content: msg.content });
    }

    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${this.apiKey}`
        },
        body: JSON.stringify({
          model: targetModel,
          messages: formattedMessages,
          temperature,
          max_tokens: maxTokens,
          stream: true
        }),
        signal: controller.signal
      });

      if (!response.ok) {
        clearTimeout(timer);
        const data = await response.json().catch(() => ({}));
        const errorMsg = data?.error?.message || `HTTP ${response.status}: Failed to stream from OpenAI`;
        const err = new Error(errorMsg);
        err.status = response.status;
        err.code = data?.error?.code || 'OPENAI_ERROR';
        if (onError) onError(err);
        throw err;
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';
      let accumulatedText = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() || '';

        for (const line of lines) {
          const trimmed = line.trim();
          if (trimmed.startsWith('data: ')) {
            const dataStr = trimmed.slice(6).trim();
            if (dataStr === '[DONE]') continue;
            try {
              const parsed = JSON.parse(dataStr);
              const chunk = parsed.choices?.[0]?.delta?.content || '';
              if (chunk) {
                accumulatedText += chunk;
                if (onChunk) onChunk(chunk);
              }
            } catch (e) {}
          }
        }
      }

      clearTimeout(timer);
      const summary = {
        text: accumulatedText,
        model: targetModel,
        usage: { promptTokens: 0, completionTokens: 0, totalTokens: 0 }
      };
      if (onComplete) onComplete(summary);
      return summary;
    } catch (err) {
      clearTimeout(timer);
      if (onError) onError(err);
      throw err;
    }
  }
}

module.exports = OpenAIProvider;
