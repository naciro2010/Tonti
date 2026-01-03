package com.tonti.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "membres",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["user_id", "daret_id"])
    ],
    indexes = [
        Index(name = "idx_membres_daret_id", columnList = "daret_id"),
        Index(name = "idx_membres_user_id", columnList = "user_id")
    ]
)
class Membre(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daret_id", nullable = false)
    var daret: Daret,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: MembreRole = MembreRole.MEMBRE,

    @Column
    var position: Int? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "joined_at", nullable = false)
    var joinedAt: Instant = Instant.now(),

    @Column(name = "left_at")
    var leftAt: Instant? = null
) : BaseEntity() {

    @OneToMany(mappedBy = "receveur", cascade = [CascadeType.ALL])
    var roundsReceived: MutableList<Round> = mutableListOf()

    fun isCreateur(): Boolean = role == MembreRole.CREATEUR
    fun isAdmin(): Boolean = role == MembreRole.ADMIN || role == MembreRole.CREATEUR
}
