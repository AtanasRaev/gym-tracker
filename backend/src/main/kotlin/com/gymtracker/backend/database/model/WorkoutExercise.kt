package com.gymtracker.backend.database.model

import jakarta.persistence.*

@Entity
@Table(name = "workout_exercises")
class WorkoutExercise(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    var exercise: Exercise,

    @OneToMany(
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    )
    @JoinColumn(name = "workout_exercise_id")
    var sets: MutableList<SetExercise> = mutableListOf()
)