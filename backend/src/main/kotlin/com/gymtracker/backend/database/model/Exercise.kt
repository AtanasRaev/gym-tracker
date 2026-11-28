package com.gymtracker.backend.database.model

import com.gymtracker.backend.database.model.enums.ExerciseCategory
import com.gymtracker.backend.database.model.enums.ExerciseEquipment
import com.gymtracker.backend.database.model.enums.ExerciseForce
import com.gymtracker.backend.database.model.enums.ExerciseLevel
import com.gymtracker.backend.database.model.enums.ExerciseMechanic
import jakarta.persistence.*


@Entity
@Table(name = "exercises")
class Exercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String,

    @ManyToMany
    @JoinTable(
        name = "exercise_primary_muscles",
        joinColumns = [JoinColumn(name = "exercise_id")],
        inverseJoinColumns = [JoinColumn(name = "muscle_group_id")]
    )
    var primaryMuscles: MutableSet<MuscleGroup> = mutableSetOf(),

    @ManyToMany
    @JoinTable(
        name = "exercise_secondary_muscles",
        joinColumns = [JoinColumn(name = "exercise_id")],
        inverseJoinColumns = [JoinColumn(name = "muscle_group_id")]
    )
    var secondaryMuscles: MutableSet<MuscleGroup> = mutableSetOf(),

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment")
    var equipment: ExerciseEquipment? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "force")
    var force: ExerciseForce? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "mechanic")
    var mechanic: ExerciseMechanic? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var category: ExerciseCategory? = null,

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @JoinColumn(name = "exercise_id")
    @OrderBy("position ASC")
    var images: MutableList<ExerciseImage> = mutableListOf(),

    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    var level: ExerciseLevel? = null,

    @Column(columnDefinition = "TEXT")
    var instructions: String? = null,

    var externalId: String? = null,

    var externalSource: String? = null
)