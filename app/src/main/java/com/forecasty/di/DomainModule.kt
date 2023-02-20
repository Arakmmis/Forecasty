package com.forecasty.di

import android.content.Context
import androidx.room.Room
import com.forecasty.domain.ForecastManager
import com.forecasty.domain.ForecastManagerImpl
import com.forecasty.domain.local.DbConfig
import com.forecasty.domain.local.ForecastDao
import com.forecasty.domain.local.ForecastDb
import com.forecasty.domain.remote.Api
import com.forecasty.domain.remote.ForecastRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DomainModule {

    @Provides
    @Singleton
    fun provideForecastManager(
        repo: ForecastRepository,
        dao: ForecastDao
    ): ForecastManager =
        ForecastManagerImpl(repo, dao)

    @Provides
    @Singleton
    fun provideForecastRepository(api: Api) = ForecastRepository(api)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ForecastDb =
        Room.databaseBuilder(
            context,
            ForecastDb::class.java,
            DbConfig.Constants.DB_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    fun provideDao(database: ForecastDb): ForecastDao =
        database.forecastDao()
}