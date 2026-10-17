/**
 * Input Validation Middleware
 * Validates chat completion and task creation payloads.
 */

const VALID_ROLES = new Set(['user', 'model', 'assistant', 'system']);
const MAX_PROMPT_CHARS = 50000;

function validateChatRequest(req, res, next) {
  const body = req.body;
  if (!body || typeof body !== 'object') {
    return res.status(400).json({
      error: {
        code: 'INVALID_INPUT',
        message: 'Request body must be a valid JSON object.',
        status: 400
      }
    });
  }

  // Normalize prompt into messages if passed as simple prompt
  if (typeof body.prompt === 'string' && (!body.messages || body.messages.length === 0)) {
    body.messages = [{ role: 'user', content: body.prompt }];
  }

  if (!Array.isArray(body.messages) || body.messages.length === 0) {
    return res.status(400).json({
      error: {
        code: 'INVALID_INPUT',
        message: 'Field "messages" must be a non-empty array of message objects, or "prompt" must be provided.',
        status: 400
      }
    });
  }

  const errors = [];

  for (let i = 0; i < body.messages.length; i++) {
    const msg = body.messages[i];
    if (!msg || typeof msg !== 'object') {
      errors.push(`messages[${i}] must be an object.`);
      continue;
    }

    if (!msg.role || !VALID_ROLES.has(msg.role.toLowerCase())) {
      errors.push(`messages[${i}].role must be one of: user, model, assistant, system.`);
    }

    if (typeof msg.content !== 'string' || msg.content.trim().length === 0) {
      errors.push(`messages[${i}].content must be a non-empty string.`);
    } else if (msg.content.length > MAX_PROMPT_CHARS) {
      errors.push(`messages[${i}].content exceeds maximum limit of ${MAX_PROMPT_CHARS} characters.`);
    }
  }

  if (body.temperature !== undefined) {
    const temp = Number(body.temperature);
    if (isNaN(temp) || temp < 0 || temp > 2.0) {
      errors.push('Field "temperature" must be a float between 0.0 and 2.0.');
    }
  }

  if (body.maxTokens !== undefined) {
    const maxT = Number(body.maxTokens);
    if (isNaN(maxT) || maxT < 1 || maxT > 32768) {
      errors.push('Field "maxTokens" must be an integer between 1 and 32768.');
    }
  }

  if (errors.length > 0) {
    return res.status(400).json({
      error: {
        code: 'INVALID_INPUT',
        message: 'Request validation failed.',
        details: errors,
        status: 400
      }
    });
  }

  next();
}

function validateTaskRequest(req, res, next) {
  const { title, objective } = req.body || {};
  const errors = [];

  if (!title || typeof title !== 'string' || title.trim().length === 0) {
    errors.push('Field "title" is required and must be a non-empty string.');
  }

  if (!objective || typeof objective !== 'string' || objective.trim().length === 0) {
    errors.push('Field "objective" is required and must be a non-empty string.');
  }

  if (errors.length > 0) {
    return res.status(400).json({
      error: {
        code: 'INVALID_INPUT',
        message: 'Task validation failed.',
        details: errors,
        status: 400
      }
    });
  }

  next();
}

module.exports = {
  validateChatRequest,
  validateTaskRequest
};
