package com.gymtracker.backend.database.model

import jakarta.persistence.*

@Entity
@Table(name = "equipment")
class Equipment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null
)