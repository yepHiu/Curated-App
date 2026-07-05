package dev.curated.app.di

import android.content.Context
import android.os.Build
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.curated.app.api.JellyfinApi
import dev.curated.app.curated.api.CuratedCookieJar
import dev.curated.app.curated.api.CuratedOkHttpClientFactory
import dev.curated.app.curated.repository.CuratedRepositoryFactory
import dev.curated.app.data.BuildConfig as DataBuildConfig
import dev.curated.app.database.ServerDatabaseDao
import dev.curated.app.settings.domain.AppPreferences
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Singleton
    @Provides
    fun provideCuratedCookieJar(): CuratedCookieJar = CuratedCookieJar()

    @Singleton
    @Provides
    fun provideCuratedOkHttpClient(
        cookieJar: CuratedCookieJar,
        appPreferences: AppPreferences,
    ): OkHttpClient =
        CuratedOkHttpClientFactory.create(
            cookieJar = cookieJar,
            clientVersion = DataBuildConfig.VERSION_NAME,
            osVersion = Build.VERSION.RELEASE,
            requestTimeoutMillis = appPreferences.getValue(appPreferences.requestTimeout),
            connectTimeoutMillis = appPreferences.getValue(appPreferences.connectTimeout),
            socketTimeoutMillis = appPreferences.getValue(appPreferences.socketTimeout),
        )

    @Singleton
    @Provides
    fun provideCuratedRepositoryFactory(
        appPreferences: AppPreferences,
        database: ServerDatabaseDao,
        curatedHttpClient: OkHttpClient,
    ): CuratedRepositoryFactory =
        CuratedRepositoryFactory(
            appPreferences = appPreferences,
            database = database,
            client = curatedHttpClient,
        )

    @Singleton
    @Provides
    fun provideJellyfinApi(
        @ApplicationContext application: Context,
        appPreferences: AppPreferences,
        database: ServerDatabaseDao,
    ): JellyfinApi {
        val jellyfinApi =
            JellyfinApi.getInstance(
                context = application,
                requestTimeout = appPreferences.getValue(appPreferences.requestTimeout),
                connectTimeout = appPreferences.getValue(appPreferences.connectTimeout),
                socketTimeout = appPreferences.getValue(appPreferences.socketTimeout),
            )

        val serverId = appPreferences.getValue(appPreferences.currentServer) ?: return jellyfinApi

        val serverWithAddressAndUser =
            database.getServerWithAddressAndUser(serverId) ?: return jellyfinApi
        val serverAddress = serverWithAddressAndUser.address ?: return jellyfinApi
        val user = serverWithAddressAndUser.user

        jellyfinApi.apply {
            api.update(baseUrl = serverAddress.address, accessToken = user?.accessToken)
            userId = user?.id
        }

        return jellyfinApi
    }
}
