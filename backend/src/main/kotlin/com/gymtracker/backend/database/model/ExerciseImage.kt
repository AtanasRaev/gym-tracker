package com.gymtracker.backend.database.model

import jakarta.persistence.*

@Entity
@Table(
    name = "exercise_images",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["public_id"])
    ]
)
class ExerciseImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "public_id", nullable = false)
    var publicId: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var url: String,

    @Column(nullable = false)
    var position: Int = 0
)
