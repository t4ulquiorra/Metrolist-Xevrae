package com.metrolist.music.di

import android.content.Context
import com.metrolist.music.domain.manager.DataStoreManager
import com.metrolist.music.service.backup.AutoBackupScheduler
import com.metrolist.music.spotify.SpotifyClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object XevraeModule {

    @Provides
    @Singleton
    fun provideSpotifyClient(): SpotifyClient = SpotifyClient()

    @Provides
    @Singleton
    fun provideBackupScheduler(
        @ApplicationContext context: Context,
        dataStoreManager: DataStoreManager
    ): AutoBackupScheduler = AutoBackupScheduler(context, dataStoreManager)
}
