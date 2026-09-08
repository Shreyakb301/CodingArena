package com.codingarena.domain.usecase

import com.codingarena.core.common.TimeProvider
import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.engine.RatingEngine
import com.codingarena.domain.model.OnboardingAnswers
import com.codingarena.domain.model.StreakState
import com.codingarena.domain.model.UserProfile
import com.codingarena.domain.repository.ProfileRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.StreakRepository

/**
 * Bridges a server session to a local one.
 *
 * Signing in with Google (or email, from the onboarding screen) hands the app a
 * token but no local profile, so the splash screen would send the user back
 * through onboarding. When a token is already in place this makes sure there is
 * a profile for it, with default preferences the user can change later in
 * Settings. Cross-device progress then arrives through the snapshot sync.
 *
 * A no-op when there is no session or a profile already exists.
 */
class EstablishSignedInProfileUseCase(
    private val gateway: ClassroomGateway,
    private val profiles: ProfileRepository,
    private val ratings: RatingRepository,
    private val streaks: StreakRepository,
    private val time: TimeProvider,
    private val ratingEngine: RatingEngine = RatingEngine(),
) {
    suspend operator fun invoke(): UserProfile? {
        val identity = gateway.refreshIdentity() ?: return profiles.current()
        profiles.current()?.let { return it }

        val now = time.nowMillis()
        val answers = OnboardingAnswers()
        val profile = UserProfile(
            id = identity.userId,
            displayName = identity.displayName.ifBlank { "You" },
            isGuest = false,
            onboarding = answers,
            createdAt = now,
            startingRating = answers.experienceLevel.startingRating,
        )
        profiles.save(profile)
        ratings.save(profile.id, ratingEngine.seedRatings(profile.startingRating, emptySet(), now))
        streaks.save(profile.id, StreakState(weeklyGoalDays = answers.weeklyGoalDays))
        return profile
    }
}
