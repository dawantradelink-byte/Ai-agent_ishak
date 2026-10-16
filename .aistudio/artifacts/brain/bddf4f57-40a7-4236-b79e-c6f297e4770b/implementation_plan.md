# My AI - Mobile-First Conversational Assistant

A warm-editorial, mobile-first AI chatbot inspired by ChatGPT usability, powered by Google Gemini API and local Room database storage. Supports natural bilingual conversations in English and Bengali (বাংলা), responsive chat history drawer, message actions (copy, retry, stop), and formatted code blocks.

### User Review & Critical Decisions

> [!IMPORTANT]
> The choices confirmed during clarification are locked in and incorporated below:
> - **Theme Style**: Warm editorial aesthetic with cream canvas (`#FAF9F5`), warm coral primary accents (`#CC785C`), and dark surfaces (`#181715`) for code blocks and mockup surfaces.
> - **Chat History**: Saved locally and privately on device using Room Database with an interactive conversation history drawer.
> - **Assistant Persona**: Balanced bilingual assistant in English and Bengali (বাংলা) with contextual system instructions.
> - **API Key Security & Cost**: Operates entirely within the free-tier quota of Google AI Studio via `BuildConfig.GEMINI_API_KEY` (configured via Secrets panel).

---

### 1. Overview & Core Concept

- **What It Does**: "My AI" is a mobile AI chatbot companion that lets users have multi-turn conversations, ask questions, draft content, and write code in English, Bengali, or mixed language. Past conversations are automatically saved in a sidebar drawer for instant recall.
- **Target Audience**: Users looking for a fast, elegant, privacy-respecting AI chat app on Android that feels handcrafted, literary, and responsive.
- **Key Value**: Delivers the fluid usability of ChatGPT combined with a literary warm-cream visual identity, robust offline history, and zero-cost Gemini API connectivity.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **First Launch / New Chat**:
   - Clean top bar with navigation drawer menu icon, "My AI" badge, and "New Chat" button (`+`).
   - Friendly greeting screen with warm starter prompt cards in both English and Bengali (e.g., "বাংলায় একটি গল্প লিখুন", "Explain how AI works", "Python script for data cleaning").
2. **Chatting & Streaming**:
   - User types in the bottom dock with auto-expanding input field and coral send button.
   - While generating, a "Stop" button appears, allowing the user to halt response generation mid-way.
   - Real-time response bubbles appear with Markdown support and syntax-highlighted dark code blocks.
3. **Message Interaction**:
   - Quick action bar below AI messages: "Copy text" icon with instant toast feedback, and "Retry / Regenerate" button.
4. **Conversation Drawer (Sidebar)**:
   - Sliding navigation drawer showing previous conversation sessions ordered by timestamp.
   - Each session displays a generated title based on the first prompt, message count, and relative time.
   - Ability to tap any past chat to resume, or delete old sessions.

#### Visual Identity & Theme Tokens
- **Canvas / Background**: Warm Tinted Cream (`#FAF9F5`)
- **Primary / Brand Accent**: Signature Warm Coral (`#CC785C`), Active (`#A9583E`)
- **Card Surfaces**: Soft Sand Cream (`#EFE9DE`)
- **Dark Product & Code Surfaces**: Deep Navy Ink (`#181715`), Soft Dark (`#252320`)
- **Text & Ink**: Dark Ink (`#141413`), Body text (`#3D3D3A`), Muted (`#6C6A64`)
- **Borders & Dividers**: Hairline Cream (`#E6DFD8`)
- **Typography**: Clean serif title voice paired with humanist sans body and monospace (`JetBrains Mono` / Monospace) for code blocks.
- **Corner Radii**: 8dp for buttons/inputs, 12dp for message cards and code containers, pill (9999dp) for badges.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct Gemini REST API via Retrofit**:
  - *Chosen Approach*: Lightweight Retrofit HTTP client calling Google Gemini (`gemini-3.5-flash`) via `BuildConfig.GEMINI_API_KEY`.
  - *Why*: Fast, zero-dependency overhead, reliable coroutines support, with 60-second timeouts suitable for full multi-turn generation.
- **Decision 2: Room Local Database**:
  - *Chosen Approach*: SQLite Room DB with two entities: `ChatSession` (id, title, createdAt, updatedAt) and `ChatMessage` (id, sessionId, role, text, timestamp).
  - *Why*: Fulfills the user requirement for device-stored history without forcing user accounts, login friction, or cloud billing.
- **Decision 3: Bilingual System Instruction**:
  - *Chosen Approach*: Tailored system prompt instructing Gemini to seamlessly detect and respond in Bengali or English based on input, providing culturally authentic, polite, and well-structured replies.

---

### 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                      UI Layer                          │
│  MainActivity ──> ChatScreen (TopBar, Drawer, Bubble)  │
└──────────────────────────┬─────────────────────────────┘
                           │ StateFlow / Events
┌──────────────────────────▼─────────────────────────────┐
│                     ChatViewModel                      │
│   Manages active session, input state, streaming/stop  │
└──────────────┬──────────────────────────┬──────────────┘
               │                          │
┌──────────────▼─────────────┐   ┌────────▼──────────────┐
│       GeminiRepository     │   │     ChatRepository    │
│  Calls Gemini 3.5 Flash    │   │  Room DB (Sessions &  │
│  via Retrofit & Coroutines │   │  Messages with Flow)  │
└────────────────────────────┘   └───────────────────────┘
```

#### Entities:
1. `ChatSessionEntity`: `id: String`, `title: String`, `createdAt: Long`, `updatedAt: Long`
2. `ChatMessageEntity`: `id: String`, `sessionId: String`, `role: String` ("user" | "assistant"), `content: String`, `timestamp: Long`

#### Core Components:
- `GeminiApiService`: Retrofit interface defining the Gemini generateContent endpoint.
- `ChatDao`: Room DAO for reactive queries (`Flow<List<ChatSessionEntity>>`, `Flow<List<ChatMessageEntity>>`).
- `MarkdownText`: Composable parser rendering headings, lists, bold text, and dark code blocks with copy functionality.
- `ChatDrawer`: Navigation drawer displaying past chat history and new chat trigger.
