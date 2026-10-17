const GeminiProvider = require('./GeminiProvider');
const OpenAIProvider = require('./OpenAIProvider');

/**
 * Provider Registry - Manages multiple AI model providers,
 * routes model requests, and provides fallback and status information.
 */
class ProviderRegistry {
  constructor() {
    this.providers = new Map();
    this.init();
  }

  init() {
    // 1. Google Gemini Provider
    const geminiKey = process.env.GEMINI_API_KEY || '';
    const gemini = new GeminiProvider({ apiKey: geminiKey });
    this.register('gemini', gemini);

    // 2. OpenAI Provider
    const openAiKey = process.env.OPENAI_API_KEY || '';
    const openai = new OpenAIProvider({ apiKey: openAiKey });
    this.register('openai', openai);
  }

  register(name, provider) {
    this.providers.set(name.toLowerCase(), provider);
  }

  get(name) {
    return this.providers.get(name.toLowerCase());
  }

  /**
   * Determine provider for a requested model or provider name
   * @param {string} [modelOrProvider]
   * @returns {import('./BaseProvider')}
   */
  resolveProvider(modelOrProvider) {
    if (!modelOrProvider) {
      return this.getActiveProvider();
    }

    const key = modelOrProvider.toLowerCase();

    // Direct provider name match
    if (this.providers.has(key)) {
      const p = this.providers.get(key);
      if (p.isAvailable()) return p;
    }

    // Model prefix routing
    if (key.startsWith('gemini')) {
      const p = this.providers.get('gemini');
      if (p.isAvailable()) return p;
    }

    if (key.startsWith('gpt')) {
      const p = this.providers.get('openai');
      if (p.isAvailable()) return p;
    }

    return this.getActiveProvider();
  }

  /**
   * Get the primary active provider that has credentials configured
   * @returns {import('./BaseProvider') | null}
   */
  getActiveProvider() {
    const preferred = process.env.DEFAULT_PROVIDER?.toLowerCase();
    if (preferred && this.providers.has(preferred)) {
      const p = this.providers.get(preferred);
      if (p.isAvailable()) return p;
    }

    // Default preference: Gemini (free tier compatible, 15 RPM / 1M TPM)
    const gemini = this.providers.get('gemini');
    if (gemini && gemini.isAvailable()) {
      return gemini;
    }

    const openai = this.providers.get('openai');
    if (openai && openai.isAvailable()) {
      return openai;
    }

    return null;
  }

  /**
   * List all providers with availability status and supported models
   */
  listProviders() {
    const list = [];
    for (const [name, provider] of this.providers.entries()) {
      list.push({
        name,
        isConfigured: provider.isAvailable(),
        defaultModel: provider.getDefaultModel(),
        models: provider.getSupportedModels()
      });
    }
    return list;
  }
}

module.exports = new ProviderRegistry();
