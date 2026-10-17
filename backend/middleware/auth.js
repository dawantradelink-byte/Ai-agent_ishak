/**
 * Authentication Middleware
 * Enforces bearer token or API key authentication against BACKEND_AUTH_SECRET.
 */
function authMiddleware(req, res, next) {
  // Allow health check and provider inspection without auth
  if (req.path === '/api/health' || req.path === '/api/providers') {
    return next();
  }

  const authSecret = process.env.BACKEND_AUTH_SECRET;

  // Extract token from Authorization header or x-api-key
  const authHeader = req.headers['authorization'] || '';
  const xApiKey = req.headers['x-api-key'] || '';

  let clientToken = '';
  if (authHeader.startsWith('Bearer ')) {
    clientToken = authHeader.slice(7).trim();
  } else if (authHeader.startsWith('ApiKey ')) {
    clientToken = authHeader.slice(7).trim();
  } else if (xApiKey) {
    clientToken = xApiKey.trim();
  }

  // If a secret is configured on the server, enforce strict match
  if (authSecret && authSecret.trim().length > 0) {
    if (!clientToken || clientToken !== authSecret.trim()) {
      return res.status(401).json({
        error: {
          code: 'UNAUTHORIZED',
          message: 'Invalid or missing authorization credentials. Pass valid Bearer token or x-api-key header.',
          status: 401
        }
      });
    }
    req.auth = { authenticated: true, mode: 'TOKEN_VERIFIED' };
    return next();
  }

  // If no server secret configured, default to internal development mode
  req.auth = { authenticated: true, mode: 'DEV_ENVIRONMENT' };
  next();
}

module.exports = authMiddleware;
