package com.tonti.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant

@Entity
@Table(
    name = "rounds",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["daret_id", "numero"])
    ],
    indexes = [
        Index(name = "idx_rounds_daret_id", columnList = "daret_id"),
        Index(name = "idx_rounds_receveur_id", columnList = "receveur_id")
    ]
)
class Round(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daret_id", nullable = false)
    var daret: Daret,

    @Column(nullable = false)
    var numero: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receveur_id", nullable = false)
    var receveur: Membre,

    @Column(name = "date_debut", nullable = false)
    var dateDebut: Instant,

    @Column(name = "date_fin", nullable = false)
    var dateFin: Instant,

    @Column(name = "est_clos", nullable = false)
    var estClos: Boolean = false,

    @Column(name = "closed_at")
    var closedAt: Instant? = null,

    @Column(name = "montant_total", precision = 12, scale = 2)
    var montantTotal: BigDecimal = BigDecimal.ZERO
) : BaseEntity() {

    @OneToMany(mappedBy = "round", cascade = [CascadeType.ALL])
    var payments: MutableList<Payment> = mutableListOf()

    fun close() {
        estClos = true
        closedAt = Instant.now()
    }
}
