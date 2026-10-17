/**
 * In-Memory Sliding Window Rate Limiter Middleware
 * Tracks requests per IP address / client identifier to prevent abuse and API exhaustion.
 */
class RateLimiter {
  constructor(options = {}) {
    this.windowMs = options.windowMs || 60 * 1000; // 1 minute window
    this.maxRequests = options.maxRequests || 60;   // 60 requests / minute
    this.clients = new Map();

    // Clean up stale client records every 5 minutes
    setInterval(() => this.cleanup(), 5 * 60 * 1000).unref();
  }

  middleware() {
    return (req, res, next) => {
      // Skip health check
      if (req.path === '/api/health') return next();

      const clientId = req.ip || req.headers['x-forwarded-for'] || 'default_client';
      const now = Date.now();

      let clientRecord = this.clients.get(clientId);
      if (!clientRecord) {
        clientRecord = { timestamps: [] };
        this.clients.set(clientId, clientRecord);
      }

      // Filter out timestamps outside the active window
      clientRecord.timestamps = clientRecord.timestamps.filter(t => now - t < this.windowMs);

      const currentCount = clientRecord.timestamps.length;

      res.setHeader('X-RateLimit-Limit', this.maxRequests);
      res.setHeader('X-RateLimit-Remaining', Math.max(0, this.maxRequests - currentCount - 1));

      if (currentCount >= this.maxRequests) {
        const oldestTimestamp = clientRecord.timestamps[0];
        const retryAfterSeconds = Math.ceil((oldestTimestamp + this.windowMs - now) / 1000);

        res.setHeader('Retry-After', retryAfterSeconds);
        return res.status(429).json({
          error: {
            code: 'RATE_LIMIT_EXCEEDED',
            message: `Rate limit of ${this.maxRequests} requests per minute exceeded. Please retry after ${retryAfterSeconds}s.`,
            retryAfterSeconds,
            status: 429
          }
        });
      }

      clientRecord.timestamps.push(now);
      next();
    };
  }

  cleanup() {
    const now = Date.now();
    for (const [clientId, record] of this.clients.entries()) {
      record.timestamps = record.timestamps.filter(t => now - t < this.windowMs);
      if (record.timestamps.length === 0) {
        this.clients.delete(clientId);
      }
    }
  }
}

module.exports = new RateLimiter();
