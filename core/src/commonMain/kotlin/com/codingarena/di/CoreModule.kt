package com.codingarena.di

import com.codingarena.content.AchievementCatalogue
import com.codingarena.core.common.IdGenerator
import com.codingarena.core.common.RandomIdGenerator
import com.codingarena.core.common.SystemTimeProvider
import com.codingarena.core.common.TimeProvider
import com.codingarena.core.database.DatabaseDriverFactory
import com.codingarena.core.database.createArenaDatabase
import com.codingarena.data.repository.LocalAchievementRepository
import com.codingarena.data.repository.LocalAttemptRepository
import com.codingarena.data.repository.LocalCodeRushRepository
import com.codingarena.data.repository.LocalInterviewProgressRepository
import com.codingarena.data.repository.LocalPracticeStateRepository
import com.codingarena.data.repository.LocalCodeDraftRepository
import com.codingarena.data.repository.LocalCourseProgressRepository
import com.codingarena.data.repository.LocalCurriculumRepository
import com.codingarena.data.repository.LocalDailyPuzzleRepository
import com.codingarena.data.repository.LocalLearningPathRepository
import com.codingarena.data.repository.LocalProblemRepository
import com.codingarena.data.repository.LocalProfileRepository
import com.codingarena.data.repository.LocalRatingRepository
import com.codingarena.data.repository.LocalReviewRepository
import com.codingarena.data.repository.LocalSettingsRepository
import com.codingarena.data.remote.OfflineOnlyRemoteDataSource
import com.codingarena.data.remote.ArenaServerConfig
import com.codingarena.data.remote.KtorSubmissionGateway
import com.codingarena.data.remote.KtorClassroomGateway
import com.codingarena.data.remote.createArenaHttpClient
import com.codingarena.data.repository.LocalStreakRepository
import com.codingarena.db.ArenaDatabase
import com.codingarena.domain.engine.AchievementEngine
import com.codingarena.domain.engine.BlitzEngine
import com.codingarena.domain.engine.CodeRushEngine
import com.codingarena.domain.engine.CourseProgressEngine
import com.codingarena.domain.engine.InterviewEngine
import com.codingarena.domain.engine.LearningPathEngine
import com.codingarena.domain.engine.PlacementTestEngine
import com.codingarena.domain.engine.PracticeWorkoutEngine
import com.codingarena.domain.engine.ProblemRecommender
import com.codingarena.domain.engine.RatingEngine
import com.codingarena.domain.engine.ReadinessEngine
import com.codingarena.domain.engine.SolutionReviewEngine
import com.codingarena.domain.engine.SpacedRepetitionEngine
import com.codingarena.domain.engine.StreakEngine
import com.codingarena.domain.repository.AchievementRepository
import com.codingarena.domain.repository.AttemptRepository
import com.codingarena.domain.repository.CodeRushRepository
import com.codingarena.domain.repository.PracticeStateRepository
import com.codingarena.domain.repository.CodeDraftRepository
import com.codingarena.domain.repository.CourseProgressRepository
import com.codingarena.domain.repository.CurriculumRepository
import com.codingarena.domain.repository.DailyPuzzleRepository
import com.codingarena.domain.repository.InterviewProgressRepository
import com.codingarena.domain.repository.LearningPathRepository
import com.codingarena.domain.repository.ProblemRepository
import com.codingarena.domain.repository.ProfileRepository
import com.codingarena.domain.repository.RatingRepository
import com.codingarena.domain.repository.ReviewRepository
import com.codingarena.domain.repository.SettingsRepository
import com.codingarena.domain.repository.StreakRepository
import com.codingarena.domain.sync.ArenaRemoteDataSource
import com.codingarena.domain.sync.SyncUseCase
import com.codingarena.domain.submission.SubmissionGateway
import com.codingarena.domain.classroom.ClassroomGateway
import com.codingarena.domain.usecase.CompleteOnboardingUseCase
import com.codingarena.domain.usecase.CurrentUser
import com.codingarena.domain.usecase.EnsureDailyPuzzleUseCase
import com.codingarena.domain.usecase.GetHomeSnapshotUseCase
import com.codingarena.domain.usecase.RefreshLearningPathUseCase
import com.codingarena.domain.usecase.StartAppUseCase
import com.codingarena.domain.usecase.SubmitAnswerUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

val ioDispatcherQualifier = named("io")

/**
 * Shared object graph.
 *
 * The platform module supplies only [DatabaseDriverFactory]; everything above
 * it is common, so iOS and Android run byte-for-byte the same wiring.
 */
val coreModule: Module = module {

    single<CoroutineDispatcher>(ioDispatcherQualifier) { Dispatchers.Default }

    single<ArenaDatabase> { createArenaDatabase(get<DatabaseDriverFactory>()) }

    single<TimeProvider> { SystemTimeProvider() }
    single<IdGenerator> { RandomIdGenerator() }
    single { ArenaServerConfig() }
    single { createArenaHttpClient() }

    // ---- repositories ----
    single<ProblemRepository> { LocalProblemRepository(get(), get(ioDispatcherQualifier)) }
    single<ProfileRepository> { LocalProfileRepository(get(), get(ioDispatcherQualifier)) }
    single<RatingRepository> { LocalRatingRepository(get(), get(ioDispatcherQualifier)) }
    single<AttemptRepository> { LocalAttemptRepository(get(), get(ioDispatcherQualifier)) }
    single<ReviewRepository> { LocalReviewRepository(get(), get(ioDispatcherQualifier)) }
    single<StreakRepository> { LocalStreakRepository(get(), get(ioDispatcherQualifier)) }
    single<AchievementRepository> { LocalAchievementRepository(get(), get(ioDispatcherQualifier)) }
    single<DailyPuzzleRepository> { LocalDailyPuzzleRepository(get(), get(ioDispatcherQualifier)) }
    single<LearningPathRepository> { LocalLearningPathRepository(get(), get(ioDispatcherQualifier)) }
    single<CodeRushRepository> { LocalCodeRushRepository(get(), get(ioDispatcherQualifier)) }
    single<PracticeStateRepository> { LocalPracticeStateRepository(get(), get(ioDispatcherQualifier)) }
    single<InterviewProgressRepository> { LocalInterviewProgressRepository(get(), get(ioDispatcherQualifier)) }
    single<SettingsRepository> { LocalSettingsRepository(get(), get(ioDispatcherQualifier)) }
    single<CurriculumRepository> { LocalCurriculumRepository(get(), get(ioDispatcherQualifier)) }
    single<CourseProgressRepository> { LocalCourseProgressRepository(get(), get(ioDispatcherQualifier)) }
    single<CodeDraftRepository> { LocalCodeDraftRepository(get(), get(ioDispatcherQualifier)) }

    // ---- engines ----
    single { RatingEngine() }
    single { ProblemRecommender() }
    single { SolutionReviewEngine(get(), get()) }
    single { SpacedRepetitionEngine() }
    single { StreakEngine() }
    single { CodeRushEngine() }
    single { BlitzEngine() }
    single { PracticeWorkoutEngine() }
    single { InterviewEngine() }
    single { LearningPathEngine(get()) }
    single { ReadinessEngine() }
    single { PlacementTestEngine() }
    single { CourseProgressEngine() }
    single { AchievementEngine(AchievementCatalogue.achievements) }

    // ---- use cases ----
    factory { EnsureDailyPuzzleUseCase(get(), get(), get()) }
    factory {
        SubmitAnswerUseCase(
            problems = get(),
            profiles = get(),
            attempts = get(),
            ratings = get(),
            reviews = get(),
            streaks = get(),
            achievements = get(),
            dailyPuzzles = get(),
            learningPaths = get(),
            codeRush = get(),
            ratingEngine = get(),
            reviewEngine = get(),
            srsEngine = get(),
            streakEngine = get(),
            pathEngine = get(),
            achievementEngine = get(),
            time = get(),
            ids = get(),
        )
    }
    factory {
        GetHomeSnapshotUseCase(
            profiles = get(),
            problems = get(),
            ratings = get(),
            streaks = get(),
            reviews = get(),
            attempts = get(),
            learningPaths = get(),
            dailyPuzzles = get(),
            ensureDailyPuzzle = get(),
            recommender = get(),
            srsEngine = get(),
            streakEngine = get(),
            readinessEngine = get(),
            time = get(),
        )
    }
    single { CurrentUser(get()) }
    single<SubmissionGateway> {
        KtorSubmissionGateway(
            client = get(),
            baseUrl = get<ArenaServerConfig>().baseUrl,
            token = { get<SettingsRepository>().get(KtorClassroomGateway.AUTH_TOKEN) },
        )
    }
    single<ClassroomGateway> { KtorClassroomGateway(get(), get(), get()) }

    factory { CompleteOnboardingUseCase(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    factory { RefreshLearningPathUseCase(get(), get(), get(), get(), get(), get(), get()) }
    factory { StartAppUseCase(get(), get(), get()) }

    // ---- legacy attempt/rating sync ----
    // Course progress and classrooms use the Ktor gateway above. The older
    // attempt/rating contract remains offline until its routes move to Ktor.
    single<ArenaRemoteDataSource> { OfflineOnlyRemoteDataSource() }
    factory { SyncUseCase(get(), get(), get(), get(), get(), get(), get(), get()) }
}
