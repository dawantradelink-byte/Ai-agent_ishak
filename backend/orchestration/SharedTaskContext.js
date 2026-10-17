/**
 * SharedTaskContext: The collaborative memory and data bus exchanged across
 * the 5 specialized agents (Scout -> Mochi -> Forge -> Pixel -> Guardian).
 */
class SharedTaskContext {
  constructor(taskId, title, objective, traceId) {
    this.taskId = taskId;
    this.title = title;
    this.objective = objective;
    this.traceId = traceId || ('trc-' + Math.random().toString(36).substring(2, 9));
    this.createdAt = Date.now();
    this.retrievedMemories = [];
    this.stageOutputs = {};
    this.handoffLog = [];
    this.checkpoints = [];
  }

  /**
   * Set relevant prior memories retrieved from persistent database
   */
  setPriorMemories(memories) {
    this.retrievedMemories = Array.isArray(memories) ? memories : [];
  }

  /**
   * Record output from a completed stage
   */
  recordStageOutput(agentId, stageIndex, stageName, rawText, metadata = {}) {
    this.stageOutputs[agentId] = {
      agentId,
      stageIndex,
      stageName,
      rawText: rawText || '',
      timestamp: Date.now(),
      metadata: metadata || {}
    };
  }

  /**
   * Record a structured handoff between two agents
   */
  recordHandoff(fromAgent, toAgent, stageIndex, message) {
    const handoff = {
      id: 'handoff-' + Date.now() + '-' + Math.random().toString(36).substring(2, 6),
      taskId: this.taskId,
      fromAgent,
      toAgent,
      stageIndex,
      message,
      timestamp: Date.now()
    };
    this.handoffLog.push(handoff);
    return handoff;
  }

  /**
   * Synthesize accumulated context into a rich, structured prompt for the next agent
   */
  buildPromptForStage(agentId, stageIndex, stageName) {
    const agentRoles = {
      scout: 'RESEARCH & CITATIONS',
      mochi: 'STRATEGIC ARCHITECTURE & BREAKDOWN',
      forge: 'TECHNICAL IMPLEMENTATION & CODE',
      pixel: 'KINETIC UI/UX & VISUAL CRAFT',
      guardian: 'SECURITY AUDIT & COMPLIANCE SIGN-OFF'
    };

    let prompt = `========================================================\n`;
    prompt += `AUTONOMOUS MULTI-AGENT PIPELINE: STAGE ${stageIndex + 1}/5\n`;
    prompt += `CURRENT ASSIGNED AGENT: ${agentId.toUpperCase()} (${agentRoles[agentId] || 'SPECIALIST'})\n`;
    prompt += `TASK TITLE: ${this.title}\n`;
    prompt += `CORE OBJECTIVE: ${this.objective}\n`;
    prompt += `TRACE ID: ${this.traceId}\n`;
    prompt += `========================================================\n\n`;

    // 1. Retrieved Historical Memories
    if (this.retrievedMemories.length > 0) {
      prompt += `### RETRIEVED RELEVANT HISTORICAL MEMORIES (From Persistent Database):\n`;
      this.retrievedMemories.forEach((mem, idx) => {
        prompt += `[Memory #${idx + 1}] (${mem.agentId?.toUpperCase() || 'CORE'}): ${mem.memoryContent}\n`;
      });
      prompt += `\n`;
    }

    // 2. Accumulated outputs from prior agents in sequence
    prompt += `### ACCUMULATED PIPELINE WORKFLOW CONTEXT:\n`;

    if (this.stageOutputs['scout']) {
      prompt += `\n--- [STAGE 1] SCOUT RESEARCH FINDINGS ---\n`;
      prompt += `${this.stageOutputs['scout'].rawText.trim()}\n`;
    }

    if (this.stageOutputs['mochi']) {
      prompt += `\n--- [STAGE 2] MOCHI ARCHITECTURAL PLAN & WORK BREAKDOWN ---\n`;
      prompt += `${this.stageOutputs['mochi'].rawText.trim()}\n`;
    }

    if (this.stageOutputs['forge']) {
      prompt += `\n--- [STAGE 3] FORGE TECHNICAL CODE IMPLEMENTATION ---\n`;
      prompt += `${this.stageOutputs['forge'].rawText.trim()}\n`;
    }

    if (this.stageOutputs['pixel']) {
      prompt += `\n--- [STAGE 4] PIXEL UI/UX & MOTION DESIGN SPECIFICATIONS ---\n`;
      prompt += `${this.stageOutputs['pixel'].rawText.trim()}\n`;
    }

    prompt += `\n========================================================\n`;
    prompt += `YOUR DELIVERABLE AS ${agentId.toUpperCase()}:\n`;

    switch (agentId) {
      case 'scout':
        prompt += `1. Investigate the topic thoroughly and gather empirical facts, technical benchmarks, and citations.\n`;
        prompt += `2. Highlight trade-offs and edge cases.\n`;
        prompt += `3. Structure your research so Mochi can immediately create an architectural plan from it.\n`;
        break;
      case 'mochi':
        prompt += `1. Review Scout's research above.\n`;
        prompt += `2. Formulate a structured architecture blueprint and milestone work breakdown.\n`;
        prompt += `3. Define concrete interfaces, data entities, and component boundaries for Forge.\n`;
        break;
      case 'forge':
        prompt += `1. Review Mochi's plan and Scout's research above.\n`;
        prompt += `2. Write production-ready Kotlin code, Room SQLite schemas, and type-safe data access logic.\n`;
        prompt += `3. Include error handling, coroutine dispatchers, and state management.\n`;
        break;
      case 'pixel':
        prompt += `1. Review Forge's implementation and the system architecture above.\n`;
        prompt += `2. Design the visual styling adhering to the warm editorial palette (#FAF9F5 canvas, #CC785C coral accents).\n`;
        prompt += `3. Specify kinetic spring animations, typography scale, micro-interactions, and accessible touch targets.\n`;
        break;
      case 'guardian':
        prompt += `1. Perform a thorough security, compliance, and quality review of all preceding outputs (Scout, Mochi, Forge, Pixel).\n`;
        prompt += `2. Audit Android permissions (least privilege, zero broad storage), crypto key storage, and Google Play policies.\n`;
        prompt += `3. State clearly: "VERDICT: APPROVED" or "VERDICT: CHANGES REQUIRED", with an executive compliance sign-off.\n`;
        break;
    }

    prompt += `Deliver your response with clear markdown headings, bullet points, and code blocks where applicable.`;
    return prompt;
  }

  toJSON() {
    return {
      taskId: this.taskId,
      title: this.title,
      objective: this.objective,
      traceId: this.traceId,
      createdAt: this.createdAt,
      retrievedMemories: this.retrievedMemories,
      stageOutputs: this.stageOutputs,
      handoffLog: this.handoffLog,
      checkpoints: this.checkpoints
    };
  }

  static fromJSON(data) {
    const ctx = new SharedTaskContext(data.taskId, data.title, data.objective, data.traceId);
    ctx.createdAt = data.createdAt || Date.now();
    ctx.retrievedMemories = data.retrievedMemories || [];
    ctx.stageOutputs = data.stageOutputs || {};
    ctx.handoffLog = data.handoffLog || [];
    ctx.checkpoints = data.checkpoints || [];
    return ctx;
  }
}

module.exports = SharedTaskContext;
