package com.tonti.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "darets",
    indexes = [
        Index(name = "idx_darets_createur_id", columnList = "createur_id"),
        Index(name = "idx_darets_code_invitation", columnList = "code_invitation"),
        Index(name = "idx_darets_etat", columnList = "etat")
    ]
)
class Daret(
    @Column(nullable = false)
    var nom: String,

    @Column
    var description: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var devise: Currency = Currency.MAD,

    @Column(name = "montant_mensuel", nullable = false, precision = 12, scale = 2)
    var montantMensuel: BigDecimal,

    @Column(nullable = false)
    var taille: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var etat: DaretStatus = DaretStatus.RECRUTEMENT,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var visibilite: Visibility = Visibility.PRIVEE,

    @Column(name = "code_invitation", nullable = false, unique = true)
    var codeInvitation: String,

    @Column(name = "delai_grace_jours", nullable = false)
    var delaiGraceJours: Int = 3,

    @Column(name = "date_debut")
    var dateDebut: Instant? = null,

    @Column(name = "date_fin")
    var dateFin: Instant? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id", nullable = false)
    var createur: User
) : BaseEntity() {

    @OneToMany(mappedBy = "daret", cascade = [CascadeType.ALL], orphanRemoval = true)
    var membres: MutableList<Membre> = mutableListOf()

    @OneToMany(mappedBy = "daret", cascade = [CascadeType.ALL], orphanRemoval = true)
    var rounds: MutableList<Round> = mutableListOf()

    @OneToMany(mappedBy = "daret", cascade = [CascadeType.ALL])
    var payments: MutableList<Payment> = mutableListOf()

    fun currentRound(): Round? = rounds.find { !it.estClos }

    fun isRecrutement(): Boolean = etat == DaretStatus.RECRUTEMENT
    fun isActive(): Boolean = etat == DaretStatus.ACTIVE
    fun isComplete(): Boolean = etat == DaretStatus.TERMINEE
}
