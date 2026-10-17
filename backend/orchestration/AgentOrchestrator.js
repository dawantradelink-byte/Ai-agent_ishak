/**
 * AgentOrchestrator (Node.js Backend Module)
 * Bridges and exposes the multi-agent task orchestrator, sequential handoffs,
 * and durable persistent store operations.
 */
const taskOrchestrator = require('./TaskOrchestrator');
const persistentTaskStore = require('./PersistentTaskStore');

class AgentOrchestrator {
  constructor() {
    this.orchestrator = taskOrchestrator;
    this.store = persistentTaskStore;
  }

  submitTask(title, objective, initiatorAgentId = 'mochi') {
    return this.orchestrator.submitTask(title, objective, initiatorAgentId);
  }

  runPipeline(taskId) {
    return this.orchestrator.runPipeline(taskId);
  }

  cancelTask(taskId) {
    return this.orchestrator.cancelTask(taskId);
  }

  getTask(taskId) {
    return this.store.getTask(taskId);
  }

  getAllTasks() {
    return this.store.getAllTasks();
  }

  getCheckpoints(taskId) {
    return this.store.getCheckpoints(taskId);
  }

  getHandoffMessages(taskId) {
    return this.store.getHandoffMessages(taskId);
  }

  recoverDurableTasks() {
    return this.orchestrator.recoverDurableTasks();
  }
}

module.exports = new AgentOrchestrator();
