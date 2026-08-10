package com.codingarena.di

import com.codingarena.features.achievements.AchievementsViewModel
import com.codingarena.features.blitz.BlitzViewModel
import com.codingarena.features.challenge.ChallengeViewModel
import com.codingarena.features.challenge.PracticeResultStore
import com.codingarena.features.coderush.CodeRushMenuViewModel
import com.codingarena.features.coderush.CodeRushSessionViewModel
import com.codingarena.features.home.HomeViewModel
import com.codingarena.features.learningpath.LearningPathViewModel
import com.codingarena.features.onboarding.OnboardingViewModel
import com.codingarena.features.patternlibrary.PatternLibraryViewModel
import com.codingarena.features.profile.ProfileViewModel
import com.codingarena.features.ratings.RatingsViewModel
import com.codingarena.features.roadmap.RoadmapViewModel
import com.codingarena.features.settings.SettingsViewModel
import com.codingarena.features.splash.SplashViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Presentation-layer graph. Everything below it lives in [coreModule]. */
val appModule: Module = module {

    single { PracticeResultStore() }

    viewModel { SplashViewModel(get(), get()) }
    viewModel { OnboardingViewModel(get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ChallengeViewModel(get(), get(), get(), get(), get()) }
    viewModel { RatingsViewModel(get(), get()) }
    viewModel { PatternLibraryViewModel(get(), get()) }
    viewModel { LearningPathViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CodeRushMenuViewModel(get(), get(), get()) }
    viewModel { CodeRushSessionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AchievementsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { RoadmapViewModel(get(), get(), get(), get()) }
    viewModel { BlitzViewModel(get(), get(), get(), get(), get()) }
}
