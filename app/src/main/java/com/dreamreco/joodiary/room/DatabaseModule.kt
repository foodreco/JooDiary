package com.dreamreco.joodiary.room

import android.content.Context
import android.provider.ContactsContract
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context : Context
    ) = Room.databaseBuilder(
        context,
        Database::class.java,
        "diary_database"
    ).build()

    @Singleton
    @Provides
    fun provideDao(database: Database) = database.diaryDao

}