package dev.curated.app.setup.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.curated.app.api.JellyfinApi
import dev.curated.app.database.ServerDatabaseDao
import dev.curated.app.settings.domain.AppPreferences
import dev.curated.app.setup.data.SetupRepositoryImpl
import dev.curated.app.setup.domain.SetupRepository
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object SetupDataModule {
    @Singleton
    @Provides
    fun provideSetupRepository(
        jellyfinApi: JellyfinApi,
        serverDatabase: ServerDatabaseDao,
        appPreferences: AppPreferences,
        curatedHttpClient: OkHttpClient,
    ): SetupRepository {
        return SetupRepositoryImpl(
            jellyfinApi = jellyfinApi,
            database = serverDatabase,
            appPreferences = appPreferences,
            curatedHttpClient = curatedHttpClient,
        )
    }
}
