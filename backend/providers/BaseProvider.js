/**
 * Base AI Model Provider Abstraction
 * Defines the contract for all AI model providers (Gemini, OpenAI, Anthropic, etc.).
 */
class BaseProvider {
  /**
   * @param {Object} config - Provider configuration
   * @param {string} config.apiKey - API key from server environment
   * @param {string} [config.baseUrl] - Base URL for API requests
   */
  constructor(config = {}) {
    this.apiKey = config.apiKey || '';
    this.baseUrl = config.baseUrl || '';
  }

  /**
   * Provider identifier (e.g., 'gemini', 'openai')
   * @returns {string}
   */
  getName() {
    throw new Error('getName() must be implemented by subclass');
  }

  /**
   * Check if this provider has credentials configured
   * @returns {boolean}
   */
  isAvailable() {
    return Boolean(this.apiKey && this.apiKey.trim().length > 0);
  }

  /**
   * Get list of supported models for this provider
   * @returns {Array<{ id: string, name: string, contextWindow: number, isDefault?: boolean }>}
   */
  getSupportedModels() {
    throw new Error('getSupportedModels() must be implemented by subclass');
  }

  /**
   * Get default model ID for this provider
   * @returns {string}
   */
  getDefaultModel() {
    throw new Error('getDefaultModel() must be implemented by subclass');
  }

  /**
   * Generate text completion (non-streaming)
   * @param {Object} options
   * @param {Array<{ role: string, content: string }>} options.messages
   * @param {string} [options.systemInstruction]
   * @param {string} [options.model]
   * @param {number} [options.temperature]
   * @param {number} [options.maxTokens]
   * @param {number} [options.timeoutMs]
   * @returns {Promise<{ text: string, usage: { promptTokens: number, completionTokens: number, totalTokens: number }, model: string, finishReason: string }>}
   */
  async generate(options) {
    throw new Error('generate() must be implemented by subclass');
  }

  /**
   * Stream text completion (Server-Sent Events / Chunked)
   * @param {Object} options
   * @param {Array<{ role: string, content: string }>} options.messages
   * @param {string} [options.systemInstruction]
   * @param {string} [options.model]
   * @param {number} [options.temperature]
   * @param {number} [options.maxTokens]
   * @param {number} [options.timeoutMs]
   * @param {Function} options.onChunk - Called with string chunk: onChunk(textChunk)
   * @param {Function} options.onComplete - Called with summary: onComplete({ text, usage, model })
   * @param {Function} options.onError - Called on error: onError(err)
   * @returns {Promise<void>}
   */
  async stream(options) {
    throw new Error('stream() must be implemented by subclass');
  }
}

module.exports = BaseProvider;
