package com.tonti.dto.ai

import java.time.Instant
import java.util.UUID

// ============================================
// Chat Request/Response
// ============================================

data class ChatRequest(
    val message: String,
    val daretId: UUID? = null,
    val conversationHistory: List<ChatMessage> = emptyList()
)

data class ChatMessage(
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Instant = Instant.now()
)

data class ChatResponse(
    val message: String,
    val artifacts: List<ArtifactResponse> = emptyList(),
    val timestamp: Instant = Instant.now()
)

// ============================================
// Artifact Types
// ============================================

enum class ArtifactType {
    MERMAID,
    EXCALIDRAW,
    PDF,
    XLSX,
    TABLE,
    KPI
}

data class ArtifactResponse(
    val id: String = UUID.randomUUID().toString(),
    val type: ArtifactType,
    val title: String,
    val data: Any,
    val createdAt: Instant = Instant.now()
)

// ============================================
// Ollama API DTOs
// ============================================

data class OllamaRequest(
    val model: String,
    val messages: List<OllamaMessage>,
    val stream: Boolean = false,
    val format: String = "json",
    val options: OllamaOptions = OllamaOptions()
)

data class OllamaMessage(
    val role: String,
    val content: String
)

data class OllamaOptions(
    val temperature: Double = 0.3,
    val num_predict: Int = 4096
)

data class OllamaResponse(
    val model: String? = null,
    val message: OllamaMessage? = null,
    val done: Boolean = false
)

// ============================================
// AI-generated Artifact JSON structures
// ============================================

data class AiGeneratedResponse(
    val message: String = "",
    val artifacts: List<AiArtifact> = emptyList()
)

data class AiArtifact(
    val type: String = "table", // mermaid, excalidraw, pdf, xlsx, table, kpi
    val title: String = "",
    val data: Any? = null
)

// Specific artifact data shapes
data class TableData(
    val headers: List<String> = emptyList(),
    val rows: List<List<Any>> = emptyList()
)

data class KpiData(
    val label: String = "",
    val value: Any = "",
    val unit: String = "",
    val trend: String = "" // up, down, stable
)

data class MermaidData(
    val diagram: String = ""
)

data class ExcalidrawData(
    val elements: List<Map<String, Any>> = emptyList()
)
