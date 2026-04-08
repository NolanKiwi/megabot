package com.megabot.di

import android.content.Context
import androidx.room.Room
import com.megabot.data.local.db.MegaBotDatabase
import com.megabot.data.local.db.dao.CallSmsLogDao
import com.megabot.data.local.db.dao.MessageLogDao
import com.megabot.data.local.db.dao.ScriptDao
import com.megabot.data.local.prefs.AppPreferences
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
    fun provideDatabase(@ApplicationContext context: Context): MegaBotDatabase {
        return Room.databaseBuilder(
            context,
            MegaBotDatabase::class.java,
            "megabot.db"
        ).build()
    }

    @Provides
    fun provideScriptDao(db: MegaBotDatabase): ScriptDao = db.scriptDao()

    @Provides
    fun provideMessageLogDao(db: MegaBotDatabase): MessageLogDao = db.messageLogDao()

    @Provides
    fun provideCallSmsLogDao(db: MegaBotDatabase): CallSmsLogDao = db.callSmsLogDao()

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences =
        AppPreferences(context)
}
