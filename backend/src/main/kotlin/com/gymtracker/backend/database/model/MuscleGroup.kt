package com.gymtracker.backend.database.model

import jakarta.persistence.*

@Entity
@Table(name = "muscle_groups")
class MuscleGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false, unique = true)
    var name: String
)