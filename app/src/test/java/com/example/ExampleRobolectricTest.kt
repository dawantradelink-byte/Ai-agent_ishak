package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AgentMemoryEntity
import com.example.data.local.AppDatabase
import com.example.data.model.AgentTeam
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: ChatRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ChatRepository(database.chatDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Open AI Agent", appName)
    }

    @Test
    fun `verify five distinct agent identities`() {
        val agents = AgentTeam.allAgents
        assertEquals(5, agents.size)

        val names = agents.map { it.name }.toSet()
        assertEquals(5, names.size)
        assertTrue(names.contains("Mochi"))
        assertTrue(names.contains("Scout"))
        assertTrue(names.contains("Forge"))
        assertTrue(names.contains("Pixel"))
        assertTrue(names.contains("Guardian"))

        // Distinct colors
        val colors = agents.map { it.color }.toSet()
        assertEquals(5, colors.size)

        // Distinct roles
        val roles = agents.map { it.role }.toSet()
        assertEquals(5, roles.size)
    }

    @Test
    fun `verify persistent memory seeding and querying`() {
        runBlocking {
            repository.ensureDefaultMemoriesSeeded()

            val allMemories = repository.allMemories.first()
            assertTrue("Memories should be seeded into database", allMemories.isNotEmpty())

            // Check Mochi memories
            val mochiMemories = repository.getMemoriesForAgent("mochi").first()
            assertTrue(mochiMemories.isNotEmpty())

            // Insert custom user preference
            val customMemoryId = UUID.randomUUID().toString()
            val customMemory = AgentMemoryEntity(
                id = customMemoryId,
                agentId = "forge",
                category = "PREFERENCE",
                content = "Use Kotlin Flows instead of LiveData",
                isVerified = true
            )
            database.chatDao().insertMemory(customMemory)

            val forgeMemories = repository.getMemoriesStrictlyForAgent("forge").first()
            val found = forgeMemories.find { it.id == customMemoryId }
            assertNotNull(found)
            assertEquals("Use Kotlin Flows instead of LiveData", found?.content)

            // Delete memory
            repository.deleteMemory(customMemoryId)
            val afterDelete = repository.getMemoriesStrictlyForAgent("forge").first()
            assertTrue(afterDelete.none { it.id == customMemoryId })
        }
    }

    @Test
    fun `verify collaboration stage execution in template mode`() {
        runBlocking {
            val (mochiOutput, _) = repository.executeCollabStage(
                agentId = "mochi",
                objective = "Build offline SQLite encrypted cache",
                stageTitle = "Strategic Roadmap",
                previousContext = ""
            )
            assertTrue(mochiOutput.contains("Mochi"))

            val (forgeOutput, _) = repository.executeCollabStage(
                agentId = "forge",
                objective = "Build offline SQLite encrypted cache",
                stageTitle = "Kotlin Code",
                previousContext = mochiOutput
            )
            assertTrue(forgeOutput.contains("Forge"))
        }
    }

    @Test
    fun `verify capability registry reports truthful status`() {
        val capabilities = repository.getCapabilities()
        assertTrue(capabilities.isNotEmpty())

        // Check unsupported capabilities are honestly reported
        val codeSandbox = capabilities.find { it.id == "code_sandbox" }
        assertNotNull(codeSandbox)
        assertEquals(com.example.data.model.CapabilityStatus.UNSUPPORTED, codeSandbox?.status)

        val osControl = capabilities.find { it.id == "os_computer_control" }
        assertNotNull(osControl)
        assertEquals(com.example.data.model.CapabilityStatus.UNSUPPORTED, osControl?.status)

        // Check Room persistence and Vector Avatars are available
        val avatars = capabilities.find { it.id == "vector_avatars" }
        assertEquals(com.example.data.model.CapabilityStatus.AVAILABLE, avatars?.status)
    }

    @Test
    fun `verify controlled self-evolution instruction versioning`() {
        runBlocking {
            repository.ensureDefaultInstructionsSeeded()

            val activeMochi = repository.getActiveInstruction("mochi")
            assertNotNull(activeMochi)
            assertEquals(1, activeMochi?.version)
            assertTrue(activeMochi?.isPromoted == true)

            // Propose new instruction version
            val candidate = repository.proposeInstructionEvolution(
                agentId = "mochi",
                updatedPrompt = "Enhanced Mochi system prompt with stricter security gates.",
                rationale = "Improve quality score based on test evaluations",
                candidateScore = 0.94f
            )
            assertEquals(2, candidate.version)
            assertEquals(false, candidate.isPromoted)

            // Promote candidate version
            repository.promoteInstruction("mochi", candidate.id)
            val promotedActive = repository.getActiveInstruction("mochi")
            assertNotNull(promotedActive)
            assertEquals(2, promotedActive?.version)
            assertTrue(promotedActive?.isPromoted == true)

            // Rollback to v1
            activeMochi?.let { v1 ->
                repository.rollbackInstruction("mochi", v1.id)
            }
            val rolledBack = repository.getActiveInstruction("mochi")
            assertEquals(1, rolledBack?.version)
        }
    }

    @Test
    fun `verify durable task queue and multi-agent execution lifecycle`() {
        runBlocking {
            repository.ensureDefaultAutonomousDataSeeded()

            val task = repository.submitDurableTask(
                title = "Research and Implement KeyStore Encryption",
                objective = "Evaluate Android Room encryption and draft solution"
            )
            assertNotNull(task)
            assertEquals("QUEUED", task.status)
            assertTrue(task.traceId.startsWith("trc-"))

            // Verify 5 planned steps created in queue
            val steps = database.chatDao().getStepsForTaskDirect(task.id)
            assertEquals(5, steps.size)
            assertEquals("mochi", steps[0].assignedAgentId)
            assertEquals("scout", steps[1].assignedAgentId)
            assertEquals("forge", steps[2].assignedAgentId)
            assertEquals("pixel", steps[3].assignedAgentId)
            assertEquals("guardian", steps[4].assignedAgentId)

            // Run task to completion
            val completed = repository.runOrResumeDurableTask(task.id)
            assertEquals("COMPLETED", completed.status)
            assertTrue(completed.guardianApproved)
            assertTrue(completed.finalReport.contains("Autonomous AI Team Verified Deliverable"))

            // Verify deliverable file saved in cloud filesystem
            val cloudFiles = database.chatDao().getAllCloudFiles().first()
            val reportFile = cloudFiles.find { it.filePath.contains(task.traceId) }
            assertNotNull(reportFile)
        }
    }

    @Test
    fun `verify persistent cloud filesystem operations and approval gate`() {
        runBlocking {
            repository.ensureDefaultAutonomousDataSeeded()

            // Create file
            val writeResult = repository.saveCloudFile(
                path = "/workspace/src/TestService.kt",
                content = "class TestService { fun run() = true }",
                agentId = "forge"
            )
            assertTrue(writeResult is com.example.data.cloud.ToolExecutionResult.Success)

            val file = database.chatDao().getCloudFileByPath("/workspace/src/TestService.kt")
            assertNotNull(file)
            assertEquals("TestService.kt", file?.fileName)

            // Attempt delete -> requires approval
            val deleteResult = repository.deleteCloudFile(file!!.id)
            assertTrue(deleteResult is com.example.data.cloud.ToolExecutionResult.RequiresApproval)

            // Verify approval request created
            val pending = database.chatDao().getPendingApprovalRequests().first()
            val approval = pending.find { it.proposedActionPayload == file.filePath }
            assertNotNull(approval)

            // Resolve approval -> approve delete
            repository.resolveApproval(approval!!.id, isApproved = true)
            val afterDelete = database.chatDao().getCloudFileByPath(file.filePath)
            assertTrue("File should be deleted after approval", afterDelete == null)
        }
    }

    @Test
    fun `verify reusable skills workflow learning and promotion`() {
        runBlocking {
            repository.ensureDefaultAutonomousDataSeeded()

            val skills = repository.allSkills.first()
            assertTrue(skills.isNotEmpty())

            // Create new draft skill
            val draft = repository.createSkill(
                name = "Playwright Browser Testing",
                agentId = "scout",
                description = "Automates browser navigation and DOM verification",
                instructions = "Navigate to URL and extract evidence"
            )
            assertEquals("DRAFT", draft.status)

            // Promote skill
            repository.promoteSkill(draft.id)
            val promoted = database.chatDao().getSkillById(draft.id)
            assertEquals("PROMOTED", promoted?.status)

            // Rollback skill
            repository.rollbackSkill(draft.id)
            val rolledBack = database.chatDao().getSkillById(draft.id)
            assertEquals("DISABLED", rolledBack?.status)
        }
    }

    @Test
    fun `verify guardian security gatekeeper intercepts secrets`() {
        runBlocking {
            val tools = com.example.data.cloud.AutonomousTools(database.chatDao())

            // Safe content
            val (safeApproved, _) = tools.performGuardianAudit("Clean Code", "fun calc() = 42")
            assertTrue(safeApproved)

            // Leaked key content
            val (leakApproved, report) = tools.performGuardianAudit("Leaked Key", "val key = 'sk-12345678901234567890'")
            assertTrue(!leakApproved)
            assertTrue(report.contains("REJECTED"))
        }
    }
}
