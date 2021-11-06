package com.akeshishi.tracker.base.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.akeshishi.tracker.base.db.ActionDatabase
import com.akeshishi.tracker.util.Constants.DATABASE_NAME
import com.akeshishi.tracker.util.Constants.FIRST_TIME_LOGIN
import com.akeshishi.tracker.util.Constants.KEY_HEIGHT
import com.akeshishi.tracker.util.Constants.KEY_NAME
import com.akeshishi.tracker.util.Constants.KEY_WEIGHT
import com.akeshishi.tracker.util.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext appContext: Context) =
        Room.databaseBuilder(
            appContext,
            ActionDatabase::class.java,
            DATABASE_NAME
        ).build()


    @Singleton
    @Provides
    fun provideDao(db: ActionDatabase) = db.getActionDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences =
        appContext.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 0f)

    @Singleton
    @Provides
    fun provideFirstTimeToken(sharedPref: SharedPreferences) =
        sharedPref.getBoolean(FIRST_TIME_LOGIN, true)
}