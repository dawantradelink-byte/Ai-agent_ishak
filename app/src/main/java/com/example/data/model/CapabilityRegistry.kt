package com.example.data.model

enum class CapabilityStatus {
    AVAILABLE,
    NEEDS_CONFIGURATION,
    UNSUPPORTED,
    ERROR
}

data class AgentCapability(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val status: CapabilityStatus,
    val details: String
)

object CapabilityRegistry {
    fun getCapabilities(isApiKeyConfigured: Boolean): List<AgentCapability> = listOf(
        AgentCapability(
            id = "gemini_orchestrator",
            name = "Gemini AI Core Engine",
            category = "Reasoning & Models",
            description = "Google Gemini models for multi-step reasoning, plan synthesis, and agent workflows.",
            status = if (isApiKeyConfigured) CapabilityStatus.AVAILABLE else CapabilityStatus.NEEDS_CONFIGURATION,
            details = if (isApiKeyConfigured) "Active via BuildConfig.GEMINI_API_KEY (Model: gemini-3.5-flash)" else "Running in verified local template mode. Set GEMINI_API_KEY in AI Studio Secrets to connect live."
        ),
        AgentCapability(
            id = "durable_state_machine",
            name = "Durable Task State Machine",
            category = "Autonomous Systems",
            description = "Persistent task queue with 5-stage sequential handoffs, step checkpoints, and crash recovery.",
            status = CapabilityStatus.AVAILABLE,
            details = "Task state and cumulative findings are checkpointed to SQLite Room after every step."
        ),
        AgentCapability(
            id = "cloud_filesystem",
            name = "Persistent Cloud Filesystem",
            category = "Cloud Computer",
            description = "Virtual workspace filesystem supporting file creation, editing, directory listing, and artifact export.",
            status = CapabilityStatus.AVAILABLE,
            details = "Mount path /workspace with persistent storage across sessions."
        ),
        AgentCapability(
            id = "sandboxed_terminal",
            name = "Sandboxed Command Execution",
            category = "Developer Tools",
            description = "Controlled shell and script runner with Guardian security blocklist preventing destructive commands.",
            status = CapabilityStatus.AVAILABLE,
            details = "Captures stdout/stderr with security gatekeeper inspection."
        ),
        AgentCapability(
            id = "browser_automation",
            name = "Browser Automation & Evidence",
            category = "Tools & Connectors",
            description = "Automated web navigation, DOM extraction, and source-backed citation gathering via Scout.",
            status = CapabilityStatus.AVAILABLE,
            details = "Generates verifiable evidence records and DOM summaries without CAPTCHA bypass violations."
        ),
        AgentCapability(
            id = "reusable_skills",
            name = "Workflow Learning & Reusable Skills",
            category = "Autonomous Systems",
            description = "Converts task demonstrations into versioned draft skills, evaluates safety, and promotes with approval.",
            status = CapabilityStatus.AVAILABLE,
            details = "Supports versioning, required tool permissions, and rollback."
        ),
        AgentCapability(
            id = "scheduled_routines",
            name = "Scheduled Routines & Background Jobs",
            category = "Automation",
            description = "Durable cron and interval job runner with visible owner, execution history, and retry policies.",
            status = CapabilityStatus.AVAILABLE,
            details = "Configurable daily and interval triggers with active/pause toggle."
        ),
        AgentCapability(
            id = "human_approval_gate",
            name = "Human-in-the-Loop Security Gatekeeper",
            category = "Security & Governance",
            description = "Requires explicit human authorization before executing consequential actions (file deletes, elevated shell).",
            status = CapabilityStatus.AVAILABLE,
            details = "Guardian gatekeeper blocks unauthorized writes and flags potential secret leaks."
        ),
        AgentCapability(
            id = "vector_avatars",
            name = "5 Character Vector Avatars",
            category = "Identity & UI",
            description = "High-DPI vector Canvas paths for Mochi, Scout, Forge, Pixel, and Guardian.",
            status = CapabilityStatus.AVAILABLE,
            details = "Zero blurry PNGs or raster scaling; crisp rendering on all screen densities."
        ),
        AgentCapability(
            id = "voice_tts",
            name = "Android Text-to-Speech (TTS)",
            category = "Multimodal I/O",
            description = "Native Android TextToSpeech engine with distinct pitch/speech rate per agent.",
            status = CapabilityStatus.AVAILABLE,
            details = "Active via android.speech.tts.TextToSpeech."
        ),
        AgentCapability(
            id = "code_sandbox",
            name = "Direct Host Kernel Code Sandbox",
            category = "Execution Isolation",
            description = "Unrestricted host kernel execution on device.",
            status = CapabilityStatus.UNSUPPORTED,
            details = "Android sandbox restricts raw kernel operations; sandboxed cloud or applet execution used instead."
        ),
        AgentCapability(
            id = "os_computer_control",
            name = "Direct OS Device Control",
            category = "System Privileges",
            description = "Raw mouse, keyboard, and OS window hooks outside the Android sandbox.",
            status = CapabilityStatus.UNSUPPORTED,
            details = "Prohibited by Android security sandbox; browser automation and cloud VM adapters provide isolated interaction."
        ),
        AgentCapability(
            id = "remote_cloud_vm",
            name = "Remote Cloud VM Adapter",
            category = "Cloud Computer",
            description = "Deployable Docker / Node.js backend container for running 24/7 background agent jobs.",
            status = CapabilityStatus.AVAILABLE,
            details = "Complete deployment files provided in backend/ directory for Cloud Run, AWS, or local Docker."
        ),
        AgentCapability(
            id = "openai_grok_adapter",
            name = "OpenAI & Grok API Adapter",
            category = "Third-Party Providers",
            description = "Modular provider routing for OpenAI and xAI Grok endpoints via server-side configuration.",
            status = CapabilityStatus.NEEDS_CONFIGURATION,
            details = "Configure server-side OPENAI_API_KEY or XAI_API_KEY in backend/.env to route tasks."
        )
    )
}
