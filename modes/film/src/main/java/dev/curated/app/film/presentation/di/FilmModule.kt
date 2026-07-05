package dev.curated.app.film.presentation.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.curated.app.film.domain.VideoMetadataParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FilmModule {
    @Singleton
    @Provides
    fun provideVideoMetadataParser(): VideoMetadataParser {
        return VideoMetadataParser
    }
}
