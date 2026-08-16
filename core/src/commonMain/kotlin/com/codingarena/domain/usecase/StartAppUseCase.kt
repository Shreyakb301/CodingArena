package com.codingarena.domain.usecase

import com.codingarena.content.StarterContent
import com.codingarena.domain.model.UserProfile
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.repository.ProfileRepository
import com.codingarena.domain.repository.SettingsRepository

/** Where the app should land after the splash screen. */
sealed interface StartDestination {
    data object Onboarding : StartDestination
    data class Home(val profile: UserProfile) : StartDestination
}

/**
 * Splash-screen work: make sure the bundled content is in the database, then
 * decide whether this is a first run.
 *
 * Seeding here rather than at install time means the very first launch is
 * fully usable offline, with no network call and no empty state.
 */
class StartAppUseCase(
    private val problems: ProblemRepository,
    private val profiles: ProfileRepository,
    private val settings: SettingsRepository,
) {

    suspend operator fun invoke(): StartDestination {
        problems.seedIfEmpty(StarterContent.problems)

        val seededVersion = settings.get(KEY_CONTENT_VERSION)?.toIntOrNull() ?: 0
        if (seededVersion < CONTENT_VERSION) {
            // Bundled content moved on since the last launch - refresh it in
            // place. Attempts and ratings reference problems by id, so
            // upserting never orphans a user's history.
            problems.upsertAll(StarterContent.problems)
            settings.put(KEY_CONTENT_VERSION, CONTENT_VERSION.toString())
        }

        return profiles.current()
            ?.let { StartDestination.Home(it) }
            ?: StartDestination.Onboarding
    }

    companion object {
        const val KEY_CONTENT_VERSION = "content_version"

        /** Bump when the bundled problem set changes. */
        const val CONTENT_VERSION = 2
    }
}
