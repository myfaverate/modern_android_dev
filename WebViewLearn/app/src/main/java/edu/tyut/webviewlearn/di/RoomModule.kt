package edu.tyut.webviewlearn.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import edu.tyut.webviewlearn.data.local.dao.PersonDao
import edu.tyut.webviewlearn.data.local.database.AppDatabase
import javax.inject.Singleton

/**
 * @Author 张书豪
 * @Date 2024/9/20 17:24
 */
private const val DATABASE_NAME: String = "app.db"

@Module
@InstallIn(value = [SingletonComponent::class])
internal class RoomModule internal constructor(){

    @Singleton
    @Provides
    internal fun providerAppInfoDao(appDatabase: AppDatabase): PersonDao {
        return appDatabase.personDao()
    }

    @Singleton
    @Provides
    internal fun providerDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            context = application,
            klass = AppDatabase::class.java,
            name = DATABASE_NAME
        ).build()
    }

}