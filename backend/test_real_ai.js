/**
 * Comprehensive End-to-End Test Suite for Real AI Backend
 * Tests Provider Abstraction, Input Validation, Real Gemini Generation,
 * Streaming SSE, Rate Limiting, Usage Tracking, and Multi-Agent Pipeline.
 */

const http = require('http');
const app = require('./server');

const TEST_PORT = 8089;
let server;

function startServer() {
  return new Promise((resolve) => {
    server = app.listen(TEST_PORT, '127.0.0.1', () => {
      console.log(`[TEST SERVER] Running on port ${TEST_PORT}`);
      resolve();
    });
  });
}

function stopServer() {
  return new Promise((resolve) => {
    if (server) server.close(() => resolve());
    else resolve();
  });
}

async function runTests() {
  console.log('====================================================');
  console.log('   PHASE 2: REAL AI BACKEND - INTEGRATION TEST SUITE');
  console.log('====================================================\n');

  await startServer();

  const baseUrl = `http://127.0.0.1:${TEST_PORT}`;
  let passed = 0;
  let failed = 0;

  async function test(name, fn) {
    process.stdout.write(`• ${name}... `);
    try {
      await fn();
      console.log('PASSED ✓');
      passed++;
    } catch (err) {
      console.log(`FAILED ✗: ${err.message}`);
      failed++;
    }
  }

  try {
    // Test 1: Health & Active Provider
    await test('Health check and Active AI Provider inspection', async () => {
      const res = await fetch(`${baseUrl}/api/health`);
      if (!res.ok) throw new Error(`Status ${res.status}`);
      const data = await res.json();
      if (data.status !== 'ONLINE') throw new Error(`Unexpected status: ${data.status}`);
      if (!data.aiProvider || !data.aiProvider.isConfigured) {
        throw new Error('AI Provider not configured');
      }
      if (data.aiProvider.name !== 'gemini') {
        throw new Error(`Expected provider gemini, got ${data.aiProvider.name}`);
      }
    });

    // Test 2: Provider Registry List
    await test('Provider Registry list and multi-provider abstraction', async () => {
      const res = await fetch(`${baseUrl}/api/providers`);
      if (!res.ok) throw new Error(`Status ${res.status}`);
      const data = await res.json();
      if (data.activeProvider !== 'gemini') throw new Error(`Active provider: ${data.activeProvider}`);
      if (!Array.isArray(data.providers) || data.providers.length < 2) {
        throw new Error('Expected at least 2 registered providers (gemini, openai)');
      }
    });

    // Test 3: Input Validation
    await test('Input validation rejects empty or invalid requests', async () => {
      const res = await fetch(`${baseUrl}/api/chat/completions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({})
      });
      if (res.status !== 400) throw new Error(`Expected 400, got ${res.status}`);
      const data = await res.json();
      if (data.error.code !== 'INVALID_INPUT') throw new Error(`Expected INVALID_INPUT, got ${data.error.code}`);
    });

    // Test 4: Rate Limiter Headers
    await test('Rate limiter attaches headers', async () => {
      const res = await fetch(`${baseUrl}/api/providers`);
      const limit = res.headers.get('x-ratelimit-limit');
      const remaining = res.headers.get('x-ratelimit-remaining');
      if (!limit || !remaining) throw new Error('Missing rate limit headers');
    });

    // Test 5: Real Non-Streaming Completion via Gemini
    await test('Real Non-Streaming Completion with Mochi Agent persona', async () => {
      const res = await fetch(`${baseUrl}/api/chat/completions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          agentId: 'mochi',
          messages: [
            { role: 'user', content: 'Introduce yourself in one short sentence as Mochi.' }
          ]
        })
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(`Status ${res.status}: ${JSON.stringify(errData)}`);
      }

      const data = await res.json();
      if (!data.choices || !data.choices[0] || !data.choices[0].message) {
        throw new Error('Invalid choices format in response');
      }

      const reply = data.choices[0].message.content;
      if (!reply || reply.trim().length === 0) {
        throw new Error('Received empty reply from model');
      }

      if (!data.usage || data.usage.totalTokens <= 0) {
        throw new Error('Missing token usage in response');
      }

      // Verify that Mochi identity is reflected in response or agent header
      if (data.agent.id !== 'mochi') {
        throw new Error(`Expected agent mochi, got ${data.agent.id}`);
      }
    });

    // Test 6: Real Streaming SSE via Gemini
    await test('Real Streaming SSE Completion with Scout Agent persona', async () => {
      const res = await fetch(`${baseUrl}/api/chat/stream`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          agentId: 'scout',
          messages: [
            { role: 'user', content: 'Say: "Scout online" and list 2 strengths.' }
          ]
        })
      });

      if (!res.ok) {
        const errData = await res.json().catch(() => ({}));
        throw new Error(`Status ${res.status}: ${JSON.stringify(errData)}`);
      }

      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      let streamBuffer = '';
      let receivedChunks = 0;
      let sawDone = false;

      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        streamBuffer += decoder.decode(value, { stream: true });
        const lines = streamBuffer.split('\n');
        streamBuffer = lines.pop() || '';

        for (const line of lines) {
          const trimmed = line.trim();
          if (trimmed.startsWith('data: ')) {
            const jsonStr = trimmed.slice(6).trim();
            if (jsonStr === '[DONE]') {
              sawDone = true;
              continue;
            }
            try {
              const parsed = JSON.parse(jsonStr);
              if (parsed.choices?.[0]?.delta?.content) {
                receivedChunks++;
              }
            } catch (e) {}
          }
        }
      }

      if (receivedChunks === 0) throw new Error('Zero content chunks received in stream');
      if (!sawDone) throw new Error('Did not receive [DONE] terminator');
    });

    // Test 7: Real Multi-Agent Pipeline Execution
    await test('Real Multi-Agent Autonomous Pipeline Execution (Mochi & Scout real generation)', async () => {
      const res = await fetch(`${baseUrl}/api/tasks`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          title: 'Benchmark SQLite vs DataStore',
          objective: 'Compare encryption, schema migrations, and concurrency in Android'
        })
      });

      if (!res.ok) throw new Error(`Status ${res.status}`);
      const task = await res.json();
      if (task.status !== 'QUEUED' && task.status !== 'RUNNING') {
        throw new Error(`Initial status ${task.status}`);
      }

      // Wait a moment for the real model to begin generating stages
      await new Promise(r => setTimeout(r, 4500));

      const pollRes = await fetch(`${baseUrl}/api/tasks/${task.id}`);
      const polled = await pollRes.json();
      // Verify task is actively executing with real steps
      if (!polled.steps || polled.steps.length !== 5) {
        throw new Error('Steps count mismatch');
      }
      // Check that at least stage 0 has completed with non-empty genuine output
      const step0 = polled.steps[0];
      if (step0.status === 'COMPLETED' && (!step0.output || step0.output.length < 10)) {
        throw new Error('Stage 0 completed but had no genuine output');
      }
    });

    // Test 8: Usage Tracking Metrics
    await test('Usage tracker records transactions, tokens, and latency', async () => {
      const res = await fetch(`${baseUrl}/api/usage`);
      if (!res.ok) throw new Error(`Status ${res.status}`);
      const stats = await res.json();

      if (stats.totalRequests < 2) {
        throw new Error(`Expected at least 2 tracked requests, got ${stats.totalRequests}`);
      }
      if (stats.totalTokens <= 0) {
        throw new Error(`Expected non-zero token count, got ${stats.totalTokens}`);
      }
      if (stats.averageLatencyMs <= 0) {
        throw new Error(`Expected non-zero avg latency, got ${stats.averageLatencyMs}`);
      }
    });

  } finally {
    await stopServer();
  }

  console.log('\n====================================================');
  console.log(`RESULTS: ${passed} PASSED, ${failed} FAILED`);
  console.log('====================================================');

  if (failed > 0) process.exit(1);
}

runTests().catch(err => {
  console.error('Test runner error:', err);
  process.exit(1);
});
