const fs = require('fs');
const path = require('path');

/**
 * PersistentTaskStore: ACID-safe persistent storage for Tasks, Checkpoints,
 * Agent Handoff Messages, and Semantic Memories.
 * Survives server restarts and worker crashes.
 */
class PersistentTaskStore {
  constructor(storePath) {
    this.storePath = storePath || path.join(__dirname, '../orchestrator_store.json');
    this.data = {
      tasks: {},
      checkpoints: {},       // taskId -> array of checkpoints
      agent_memories: [],    // array of memory entities
      agent_messages: [],    // array of handoff messages
      task_history: []       // audit transitions
    };
    this.load();
  }

  load() {
    try {
      if (fs.existsSync(this.storePath)) {
        const raw = fs.readFileSync(this.storePath, 'utf-8');
        this.data = Object.assign(this.data, JSON.parse(raw));
      } else {
        this.seedDefaultMemories();
        this.persist();
      }
    } catch (err) {
      console.error('[PersistentTaskStore] Load error, re-initializing:', err.message);
      this.seedDefaultMemories();
      this.persist();
    }
  }

  persist() {
    const tmpPath = this.storePath + '.tmp';
    try {
      fs.writeFileSync(tmpPath, JSON.stringify(this.data, null, 2), 'utf-8');
      fs.renameSync(tmpPath, this.storePath);
    } catch (err) {
      console.error('[PersistentTaskStore] Persist error:', err.message);
    }
  }

  seedDefaultMemories() {
    this.data.agent_memories = [
      {
        id: 'mem-scout-001',
        agentId: 'scout',
        key: 'android_room_sqlite_benchmarks',
        memoryContent: 'Room SQLite database achieves sub-5ms read queries with compiled SQLiteStatement, WAL mode, and Flow observers. Preferred over raw SQLite.',
        tags: ['room', 'sqlite', 'database', 'benchmark', 'android', 'performance'],
        importance: 5,
        createdAt: Date.now() - 86400000,
        accessCount: 1
      },
      {
        id: 'mem-forge-001',
        agentId: 'forge',
        key: 'kotlin_coroutines_room_transactions',
        memoryContent: 'Room @Transaction methods must be executed with withContext(Dispatchers.IO). Never block Main thread during migrations.',
        tags: ['kotlin', 'room', 'coroutines', 'concurrency', 'transactions', 'android'],
        importance: 5,
        createdAt: Date.now() - 86400000,
        accessCount: 1
      },
      {
        id: 'mem-pixel-001',
        agentId: 'pixel',
        key: 'warm_editorial_motion_specs',
        memoryContent: 'Signature aesthetic: Tinted cream canvas (#FAF9F5), warm coral primary (#CC785C), 16.dp corner radius, and spring stiffness 300f for fluid card reveals.',
        tags: ['ui', 'design', 'warm_editorial', 'tokens', 'motion', 'compose'],
        importance: 4,
        createdAt: Date.now() - 86400000,
        accessCount: 1
      },
      {
        id: 'mem-guardian-001',
        agentId: 'guardian',
        key: 'play_policy_storage_permissions',
        memoryContent: 'Google Play 2026 Developer Policy strictly prohibits READ_EXTERNAL_STORAGE. All image/media access must use zero-permission Photo Picker contracts.',
        tags: ['security', 'permissions', 'play_policy', 'compliance', 'zero_trust'],
        importance: 5,
        createdAt: Date.now() - 86400000,
        accessCount: 1
      }
    ];
  }

  // --- Task Methods ---

  saveTask(task) {
    this.data.tasks[task.id] = {
      ...task,
      updatedAt: Date.now()
    };
    this.persist();
    return this.data.tasks[task.id];
  }

  getTask(taskId) {
    return this.data.tasks[taskId] || null;
  }

  listTasks() {
    return Object.values(this.data.tasks).sort((a, b) => (b.createdAt || 0) - (a.createdAt || 0));
  }

  updateTaskStatus(taskId, status, extra = {}) {
    const task = this.data.tasks[taskId];
    if (!task) return null;

    const oldStatus = task.status;
    task.status = status;
    task.updatedAt = Date.now();
    Object.assign(task, extra);

    this.data.task_history.push({
      taskId,
      fromStatus: oldStatus,
      toStatus: status,
      timestamp: Date.now(),
      note: extra.note || ''
    });

    this.persist();
    return task;
  }

  getIncompleteTasks() {
    return Object.values(this.data.tasks).filter(
      t => t.status === 'RUNNING' || t.status === 'CHECKPOINTED' || t.status === 'QUEUED'
    );
  }

  // --- Checkpoints Methods ---

  saveCheckpoint(taskId, checkpoint) {
    if (!this.data.checkpoints[taskId]) {
      this.data.checkpoints[taskId] = [];
    }

    const cp = {
      id: 'cp-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
      taskId,
      stageIndex: checkpoint.stageIndex,
      agentId: checkpoint.agentId,
      stageName: checkpoint.stageName,
      status: checkpoint.status || 'COMPLETED',
      output: checkpoint.output || '',
      executionTimeMs: checkpoint.executionTimeMs || 0,
      timestamp: Date.now()
    };

    // Replace if checkpoint for this stage already exists
    const existingIndex = this.data.checkpoints[taskId].findIndex(c => c.stageIndex === cp.stageIndex);
    if (existingIndex >= 0) {
      this.data.checkpoints[taskId][existingIndex] = cp;
    } else {
      this.data.checkpoints[taskId].push(cp);
    }

    this.persist();
    return cp;
  }

  getCheckpoints(taskId) {
    return this.data.checkpoints[taskId] || [];
  }

  // --- Agent Handoff Messages ---

  recordHandoffMessage(msg) {
    const handoff = {
      id: 'msg-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
      taskId: msg.taskId,
      fromAgentId: msg.fromAgentId,
      toAgentId: msg.toAgentId,
      stageIndex: msg.stageIndex,
      messageType: msg.messageType || 'HANDOFF',
      content: msg.content,
      timestamp: Date.now()
    };

    this.data.agent_messages.unshift(handoff);
    if (this.data.agent_messages.length > 200) {
      this.data.agent_messages.pop();
    }

    this.persist();
    return handoff;
  }

  getHandoffMessages(taskId) {
    return this.data.agent_messages.filter(m => m.taskId === taskId);
  }

  // --- Memory Retrieval and Storage ---

  queryMemories(queryText, agentId = null, limit = 4) {
    if (!queryText || typeof queryText !== 'string') return [];

    const words = queryText.toLowerCase()
      .replace(/[^\w\s]/g, ' ')
      .split(/\s+/)
      .filter(w => w.length > 2);

    const scored = this.data.agent_memories.map(mem => {
      let score = 0;
      const memText = (mem.key + ' ' + mem.memoryContent + ' ' + (mem.tags || []).join(' ')).toLowerCase();

      for (const w of words) {
        if (memText.includes(w)) score += 3;
      }

      if (agentId && mem.agentId === agentId) {
        score += 2;
      }

      score += (mem.importance || 1) * 0.5;

      return { mem, score };
    });

    const results = scored
      .filter(item => item.score > 0)
      .sort((a, b) => b.score - a.score)
      .slice(0, limit)
      .map(item => {
        item.mem.accessCount = (item.mem.accessCount || 0) + 1;
        return item.mem;
      });

    this.persist();
    return results;
  }

  saveMemory({ agentId, key, memoryContent, tags = [], importance = 4 }) {
    const memory = {
      id: 'mem-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
      agentId,
      key: key || ('mem_' + Date.now()),
      memoryContent,
      tags: Array.isArray(tags) ? tags : [tags],
      importance,
      createdAt: Date.now(),
      accessCount: 0
    };

    this.data.agent_memories.unshift(memory);
    if (this.data.agent_memories.length > 150) {
      this.data.agent_memories.pop();
    }

    this.persist();
    return memory;
  }

  getAllMemories() {
    return this.data.agent_memories;
  }
}

module.exports = new PersistentTaskStore();
