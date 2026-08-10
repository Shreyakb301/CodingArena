package com.codingarena

import android.app.Application
import com.codingarena.core.database.DatabaseDriverFactory
import com.codingarena.di.appModule
import com.codingarena.di.coreModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class ArenaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ArenaApplication)
            modules(
                // The only platform-specific binding in the whole graph.
                module { single { DatabaseDriverFactory(get()) } },
                coreModule,
                appModule,
            )
        }
    }
}
