package com.codingarena.domain.usecase

import com.codingarena.domain.model.UserProfile
import com.codingarena.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the signed-in (or guest) profile for the life of the process.
 *
 * Every screen needs a user id, and having each one re-read it from storage
 * would mean a dozen suspend calls before anything renders. The splash screen
 * sets this once; everything downstream reads it synchronously.
 */
class CurrentUser(private val profiles: ProfileRepository) {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    val userId: String? get() = _profile.value?.id

    fun set(profile: UserProfile?) {
        _profile.value = profile
    }

    /** Reads the stored profile if one has not been set yet. */
    suspend fun ensureLoaded(): UserProfile? {
        _profile.value?.let { return it }
        return profiles.current()?.also { _profile.value = it }
    }

    suspend fun refresh() {
        _profile.value = profiles.current()
    }
}
