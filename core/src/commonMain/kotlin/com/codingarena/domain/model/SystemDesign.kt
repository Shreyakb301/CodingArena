package com.codingarena.domain.model

/** The areas system design questions are grouped into. */
enum class SystemDesignCategory(val displayName: String) {
    INFRASTRUCTURE("Infrastructure"),
    DATABASES("Databases"),
    APIS("APIs"),
    SCALABILITY("Scalability"),
    THEORY("Theory"),
}

/** One option on a system design question. */
data class SystemDesignChoice(
    val text: String,
    val correct: Boolean,
    val explanation: String,
)

/** A short, focused question - answerable in about two minutes. */
data class SystemDesignQuestion(
    val id: String,
    val prompt: String,
    val choices: List<SystemDesignChoice>,
)

/**
 * A system design fundamental: a short explanation plus a few questions that
 * check it actually landed.
 */
data class SystemDesignConcept(
    val id: String,
    val title: String,
    val category: SystemDesignCategory,
    val summary: String,
    val keyPoints: List<String>,
    val questions: List<SystemDesignQuestion>,
)
