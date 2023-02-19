package com.forecasty.di

import com.forecasty.domain.ForecastManager
import com.forecasty.domain.ForecastManagerImpl
import com.forecasty.domain.remote.Api
import com.forecasty.domain.remote.ForecastRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DomainModule {

    @Provides
    @Singleton
    fun provideForecastManager(repo: ForecastRepository): ForecastManager =
        ForecastManagerImpl(repo)

    @Provides
    @Singleton
    fun provideForecastRepository(api: Api) = ForecastRepository(api)
}