package com.tonti.service.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tonti.config.AppProperties
import com.tonti.dto.ai.*
import com.tonti.entity.User
import com.tonti.repository.*
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import java.util.*

private val logger = KotlinLogging.logger {}

@Service
class AiAgentService(
    private val appProperties: AppProperties,
    private val objectMapper: ObjectMapper,
    private val daretRepository: DaretRepository,
    private val membreRepository: MembreRepository,
    private val roundRepository: RoundRepository,
    private val paymentRepository: PaymentRepository
) {

    private val webClient = WebClient.builder()
        .baseUrl(appProperties.ollama.baseUrl)
        .codecs { it.defaultCodecs().maxInMemorySize(16 * 1024 * 1024) }
        .build()

    fun chat(request: ChatRequest, user: User): ChatResponse {
        val contextData = buildContextData(user, request.daretId)
        val systemPrompt = buildSystemPrompt(contextData)

        val messages = mutableListOf(
            OllamaMessage(role = "system", content = systemPrompt)
        )

        // Add conversation history
        request.conversationHistory.forEach { msg ->
            messages.add(OllamaMessage(role = msg.role, content = msg.content))
        }

        // Add current user message
        messages.add(OllamaMessage(role = "user", content = request.message))

        val ollamaRequest = OllamaRequest(
            model = appProperties.ollama.model,
            messages = messages,
            stream = false,
            format = "json"
        )

        logger.info { "Sending request to Ollama (${appProperties.ollama.model}) for user ${user.id}" }

        val ollamaResponse = webClient.post()
            .uri("/api/chat")
            .bodyValue(ollamaRequest)
            .retrieve()
            .bodyToMono<OllamaResponse>()
            .timeout(Duration.ofSeconds(appProperties.ollama.timeoutSeconds))
            .block()

        val rawContent = ollamaResponse?.message?.content ?: "{\"message\": \"Désolé, je n'ai pas pu traiter votre demande.\"}"

        return parseAiResponse(rawContent)
    }

    private fun parseAiResponse(rawJson: String): ChatResponse {
        return try {
            val aiResponse = objectMapper.readValue<AiGeneratedResponse>(rawJson)
            ChatResponse(
                message = aiResponse.message,
                artifacts = aiResponse.artifacts.map { artifact ->
                    ArtifactResponse(
                        type = try { ArtifactType.valueOf(artifact.type.uppercase()) } catch (e: Exception) { ArtifactType.TABLE },
                        title = artifact.title,
                        data = artifact.data ?: emptyMap<String, Any>()
                    )
                }
            )
        } catch (e: Exception) {
            logger.warn(e) { "Failed to parse AI response as structured JSON, falling back to text" }
            ChatResponse(message = rawJson.trim('"'))
        }
    }

    // ============================================
    // Context Builder - Fetches real data for the AI
    // ============================================

    private fun buildContextData(user: User, daretId: UUID?): DaretContext {
        val userDarets = daretRepository.findByCreateur(user.id!!) +
            daretRepository.findByMemberId(user.id!!)
        val distinctDarets = userDarets.distinctBy { it.id }

        val daretSummaries = distinctDarets.map { daret ->
            val membres = membreRepository.findActiveByDaretId(daret.id!!)
            val rounds = roundRepository.findByDaretId(daret.id!!)
            val totalPaid = rounds.sumOf { round ->
                paymentRepository.findByRoundId(round.id!!).count { it.statut == com.tonti.entity.PaymentStatus.SUCCEEDED }
            }
            val totalExpected = rounds.size * (membres.size - 1)
            val completedRounds = rounds.count { it.estClos }

            DaretSummary(
                id = daret.id!!,
                nom = daret.nom,
                devise = daret.devise.name,
                montantMensuel = daret.montantMensuel,
                taille = daret.taille,
                etat = daret.etat.name,
                membresActifs = membres.size,
                totalRounds = rounds.size,
                roundsTermines = completedRounds,
                paiementsEffectues = totalPaid,
                paiementsAttendus = totalExpected,
                tauxPaiement = if (totalExpected > 0) (totalPaid.toDouble() / totalExpected * 100).toInt() else 0,
                dateDebut = daret.dateDebut,
                dateFin = daret.dateFin,
                membres = membres.map { m ->
                    MembreSummary(
                        nom = "${m.user.firstName} ${m.user.lastName}",
                        role = m.role.name,
                        position = m.position ?: 0
                    )
                }
            )
        }

        // If a specific daret is selected, get detailed info
        val selectedDaret = daretId?.let { id ->
            distinctDarets.find { it.id == id }?.let { daret ->
                val rounds = roundRepository.findByDaretId(daret.id!!)
                rounds.map { round ->
                    val payments = paymentRepository.findByRoundId(round.id!!)
                    RoundDetail(
                        numero = round.numero,
                        receveur = "${round.receveur.user.firstName} ${round.receveur.user.lastName}",
                        dateDebut = round.dateDebut,
                        dateFin = round.dateFin,
                        estClos = round.estClos,
                        paiementsRecus = payments.count { it.statut == com.tonti.entity.PaymentStatus.SUCCEEDED },
                        paiementsEnAttente = payments.count { it.statut != com.tonti.entity.PaymentStatus.SUCCEEDED },
                        montantCollecte = round.montantTotal
                    )
                }
            }
        }

        return DaretContext(
            userName = "${user.firstName} ${user.lastName}",
            darets = daretSummaries,
            selectedDaretRounds = selectedDaret,
            selectedDaretId = daretId
        )
    }

    // ============================================
    // System Prompt - Le Gestionnaire
    // ============================================

    private fun buildSystemPrompt(context: DaretContext): String {
        val daretData = if (context.darets.isNotEmpty()) {
            context.darets.joinToString("\n\n") { daret ->
                """
                |--- Daret: ${daret.nom} (${daret.id}) ---
                |État: ${daret.etat} | Devise: ${daret.devise}
                |Montant mensuel: ${daret.montantMensuel} ${daret.devise}
                |Membres: ${daret.membresActifs}/${daret.taille}
                |Rounds: ${daret.roundsTermines}/${daret.totalRounds} terminés
                |Taux de paiement: ${daret.tauxPaiement}%
                |Paiements: ${daret.paiementsEffectues}/${daret.paiementsAttendus}
                |Période: ${daret.dateDebut ?: "Non démarré"} → ${daret.dateFin ?: "N/A"}
                |Membres: ${daret.membres.joinToString(", ") { "${it.nom} (${it.role}, pos ${it.position})" }}
                """.trimMargin()
            }
        } else {
            "Aucun Daret trouvé pour cet utilisateur."
        }

        val roundsDetail = context.selectedDaretRounds?.let { rounds ->
            "\n\n--- Détails des rounds du Daret sélectionné ---\n" +
            rounds.joinToString("\n") { round ->
                "Round ${round.numero}: Receveur=${round.receveur} | ${if (round.estClos) "CLOS" else "EN COURS"} | Payés=${round.paiementsRecus} | En attente=${round.paiementsEnAttente} | Collecté=${round.montantCollecte}"
            }
        } ?: ""

        return """
Tu es un assistant de gestion intelligent spécialisé dans la gestion de tontines (Darets).
Tu es le bras droit du gestionnaire "${context.userName}". Ton rôle est d'être proactif, analytique et orienté action.

## Ton profil
- Tu es un gestionnaire financier expérimenté qui analyse les données en profondeur
- Tu fournis toujours des informations PRÉCISES, CHIFFRÉES et ACTIONNABLES
- Tu identifies les risques, les retards de paiement, les tendances
- Tu proposes des actions concrètes et des recommandations
- Tu parles en français de manière professionnelle mais accessible

## Données actuelles de l'utilisateur
Utilisateur: ${context.userName}
Nombre de Darets: ${context.darets.size}

$daretData
$roundsDetail

## Format de réponse OBLIGATOIRE
Tu DOIS répondre en JSON avec cette structure exacte:
{
  "message": "Ton analyse textuelle claire et détaillée ici",
  "artifacts": [
    {
      "type": "table|mermaid|kpi|xlsx|pdf|excalidraw",
      "title": "Titre de l'artefact",
      "data": { ... }
    }
  ]
}

## Types d'artefacts disponibles

### table - Pour les tableaux de données
{"type": "table", "title": "...", "data": {"headers": ["Col1", "Col2"], "rows": [["val1", "val2"]]}}

### kpi - Pour les indicateurs clés (toujours en inclure quand pertinent)
{"type": "kpi", "title": "...", "data": {"label": "Taux de recouvrement", "value": 85, "unit": "%", "trend": "up"}}

### mermaid - Pour les diagrammes (flux, Gantt, pie charts, séquences)
{"type": "mermaid", "title": "...", "data": {"diagram": "pie title Répartition\n\"Payé\" : 75\n\"En attente\" : 25"}}

### xlsx - Pour les exports Excel
{"type": "xlsx", "title": "...", "data": {"headers": ["Col1", "Col2"], "rows": [["val1", "val2"]], "sheetName": "Rapport"}}

### pdf - Pour les rapports PDF
{"type": "pdf", "title": "...", "data": {"sections": [{"heading": "Titre", "content": "Texte..."}], "footer": "Tonti - Rapport généré automatiquement"}}

### excalidraw - Pour les schémas visuels
{"type": "excalidraw", "title": "...", "data": {"elements": []}}

## Règles importantes
1. TOUJOURS générer au moins un artefact pertinent (KPI, tableau, diagramme)
2. Quand on te demande un état des lieux → KPIs + tableau récapitulatif + diagramme mermaid
3. Quand on te demande un rapport → PDF avec sections détaillées + XLSX pour les données
4. Quand on te demande une analyse → KPIs + mermaid pie/bar chart + tableau
5. Sois proactif: signale les retards, les risques, les membres qui n'ont pas payé
6. Utilise les données réelles fournies ci-dessus, ne fabrique JAMAIS de données
7. Si aucune donnée n'est disponible, propose de créer un Daret et explique le processus
""".trimIndent()
    }
}

// ============================================
// Internal context models
// ============================================

data class DaretContext(
    val userName: String,
    val darets: List<DaretSummary>,
    val selectedDaretRounds: List<RoundDetail>? = null,
    val selectedDaretId: UUID? = null
)

data class DaretSummary(
    val id: UUID,
    val nom: String,
    val devise: String,
    val montantMensuel: BigDecimal,
    val taille: Int,
    val etat: String,
    val membresActifs: Int,
    val totalRounds: Int,
    val roundsTermines: Int,
    val paiementsEffectues: Int,
    val paiementsAttendus: Int,
    val tauxPaiement: Int,
    val dateDebut: Instant?,
    val dateFin: Instant?,
    val membres: List<MembreSummary>
)

data class MembreSummary(
    val nom: String,
    val role: String,
    val position: Int
)

data class RoundDetail(
    val numero: Int,
    val receveur: String,
    val dateDebut: Instant,
    val dateFin: Instant,
    val estClos: Boolean,
    val paiementsRecus: Int,
    val paiementsEnAttente: Int,
    val montantCollecte: BigDecimal
)
