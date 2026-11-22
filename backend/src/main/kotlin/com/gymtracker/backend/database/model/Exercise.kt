package com.gymtracker.backend.database.model

import jakarta.persistence.*


@Entity
@Table(name = "exercises")
class Exercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @ManyToMany
    @JoinTable(
        name = "exercise_muscle_groups",
        joinColumns = [JoinColumn(name = "exercise_id")],
        inverseJoinColumns = [JoinColumn(name = "muscle_group_id")]
    )
    var muscleGroups: MutableSet<MuscleGroup> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "exercise_equipment",
        joinColumns = [JoinColumn(name = "exercise_id")],
        inverseJoinColumns = [JoinColumn(name = "equipment_id")]
    )
    var equipment: MutableSet<Equipment> = mutableSetOf(),

    @Column(columnDefinition = "TEXT")
    var imageUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    var gifUrl: String? = null,

    @Column(columnDefinition = "TEXT")
    var instructions: String? = null,

    var externalId: String? = null,

    var externalSource: String? = null
)