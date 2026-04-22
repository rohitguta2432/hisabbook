package com.hisabbook.app.di

import android.content.Context
import com.hisabbook.app.data.db.DbKeyStore
import com.hisabbook.app.data.db.HisabBookDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context, keyStore: DbKeyStore): HisabBookDb =
        HisabBookDb.build(ctx, keyStore.getOrCreate())
}
