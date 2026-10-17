const fs = require('fs');
const path = require('path');

/**
 * Usage Tracker Service
 * Tracks token usage, latency metrics, model routing statistics, and agent performance.
 */
class UsageTracker {
  constructor(filePath) {
    this.filePath = filePath || path.join(__dirname, '../usage_store.json');
    this.stats = {
      totalRequests: 0,
      successfulRequests: 0,
      failedRequests: 0,
      totalPromptTokens: 0,
      totalCompletionTokens: 0,
      totalTokens: 0,
      requestsByModel: {},
      requestsByAgent: {},
      recentTransactions: []
    };
    this.load();
  }

  load() {
    try {
      if (fs.existsSync(this.filePath)) {
        const raw = fs.readFileSync(this.filePath, 'utf-8');
        this.stats = Object.assign(this.stats, JSON.parse(raw));
      }
    } catch (err) {
      console.error('Failed to load usage_store.json:', err.message);
    }
  }

  save() {
    try {
      fs.writeFileSync(this.filePath, JSON.stringify(this.stats, null, 2), 'utf-8');
    } catch (err) {
      console.error('Failed to save usage_store.json:', err.message);
    }
  }

  recordTransaction({
    provider = 'gemini',
    model = 'gemini-2.5-flash',
    agentId = 'mochi',
    promptTokens = 0,
    completionTokens = 0,
    totalTokens = 0,
    latencyMs = 0,
    isStream = false,
    success = true,
    error = null
  }) {
    this.stats.totalRequests++;
    if (success) {
      this.stats.successfulRequests++;
    } else {
      this.stats.failedRequests++;
    }

    const effectiveTotal = totalTokens || (promptTokens + completionTokens);
    this.stats.totalPromptTokens += promptTokens;
    this.stats.totalCompletionTokens += completionTokens;
    this.stats.totalTokens += effectiveTotal;

    // By Model
    this.stats.requestsByModel[model] = (this.stats.requestsByModel[model] || 0) + 1;

    // By Agent
    this.stats.requestsByAgent[agentId] = (this.stats.requestsByAgent[agentId] || 0) + 1;

    // Recent Transaction Entry
    const tx = {
      id: 'tx-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
      timestamp: Date.now(),
      provider,
      model,
      agentId,
      promptTokens,
      completionTokens,
      totalTokens: effectiveTotal,
      latencyMs,
      isStream,
      success,
      errorMessage: error ? error.message : null
    };

    this.stats.recentTransactions.unshift(tx);
    if (this.stats.recentTransactions.length > 50) {
      this.stats.recentTransactions.pop();
    }

    this.save();
    return tx;
  }

  getStats() {
    return {
      ...this.stats,
      uptimeSeconds: Math.floor(process.uptime()),
      averageLatencyMs: this._calcAvgLatency()
    };
  }

  _calcAvgLatency() {
    if (this.stats.recentTransactions.length === 0) return 0;
    const total = this.stats.recentTransactions.reduce((acc, t) => acc + (t.latencyMs || 0), 0);
    return Math.round(total / this.stats.recentTransactions.length);
  }
}

module.exports = new UsageTracker();
