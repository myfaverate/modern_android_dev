package edu.tyut.webviewlearn.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.tyut.webviewlearn.bean.Person
import edu.tyut.webviewlearn.data.local.dao.PersonDao

/**
 * @Author 张书豪
 * @Date 2024/9/20 17:43
 */
@Database(
    entities = [Person::class],
    version = 1,
    exportSchema = false
)
internal abstract class AppDatabase internal constructor(): RoomDatabase() {
    internal abstract fun personDao(): PersonDao
}