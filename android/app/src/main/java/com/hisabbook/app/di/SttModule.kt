package com.hisabbook.app.di

import com.hisabbook.app.data.stt.SttEngine
import com.hisabbook.app.data.stt.VoskSttEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SttModule {
    @Binds
    @Singleton
    abstract fun bindSttEngine(impl: VoskSttEngine): SttEngine
}
