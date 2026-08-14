package com.codingarena.di

import com.codingarena.features.achievements.AchievementsViewModel
import com.codingarena.features.blitz.BlitzViewModel
import com.codingarena.features.blitz.StrategyBlitzViewModel
import com.codingarena.features.course.CourseRoadmapViewModel
import com.codingarena.features.course.ChapterViewModel
import com.codingarena.features.editor.CodeEditorViewModel
import com.codingarena.features.practice.PracticeViewModel
import com.codingarena.features.practice.TopicFocusViewModel
import com.codingarena.features.practice.WorkoutRunnerViewModel
import com.codingarena.features.classroom.ClassroomViewModel
import com.codingarena.features.challenge.ChallengeViewModel
import com.codingarena.features.challenge.PracticeResultStore
import com.codingarena.features.coderush.CodeRushSessionViewModel
import com.codingarena.features.home.HomeViewModel
import com.codingarena.features.interview.BehavioralRoundViewModel
import com.codingarena.features.interview.InterviewHomeViewModel
import com.codingarena.features.interview.MockInterviewViewModel
import com.codingarena.features.interview.TechCommProblemsViewModel
import com.codingarena.features.interview.TechCommRoundViewModel
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
    viewModel { OnboardingViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ChallengeViewModel(get(), get(), get(), get(), get()) }
    viewModel { RatingsViewModel(get(), get()) }
    viewModel { PatternLibraryViewModel(get(), get()) }
    viewModel { LearningPathViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CodeRushSessionViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { AchievementsViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { RoadmapViewModel(get(), get(), get(), get(), get()) }
    viewModel { BlitzViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { CourseRoadmapViewModel(get(), get(), get(), get(), get()) }
    viewModel { ChapterViewModel(get(), get(), get(), get(), get()) }
    viewModel { CodeEditorViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { ClassroomViewModel(get(), get()) }
    viewModel { StrategyBlitzViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { PracticeViewModel(get(), get(), get(), get(), get()) }
    viewModel { TopicFocusViewModel(get(), get(), get(), get()) }
    viewModel { WorkoutRunnerViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { InterviewHomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { BehavioralRoundViewModel(get(), get(), get(), get()) }
    viewModel { TechCommProblemsViewModel(get(), get()) }
    viewModel { TechCommRoundViewModel(get(), get(), get(), get()) }
    viewModel { MockInterviewViewModel(get(), get(), get(), get()) }
}
