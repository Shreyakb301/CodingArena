package com.codingarena.domain.model

import kotlinx.serialization.Serializable

/**
 * How practiced a user is *right now*, purely from Roadmap coverage (count of
 * mastered [PatternGroup] sections) - not [ExperienceLevel], which is the
 * self-reported onboarding tier used to seed a starting rating. No computed
 * skill tier exists anywhere in the codebase yet, so this is a new,
 * deliberately simple rule.
 */
@Serializable
enum class PracticeExperienceLevel {
    BEGINNER,
    PROGRESSING,
    ADVANCED,
}

/** Recommended Practice, a topic-focused Complete Workout, or Mixed Practice - the three adaptive round modes. */
@Serializable
enum class PracticeSessionKind {
    RECOMMENDED,
    TOPIC_FOCUSED,
    MIXED,
}
