package edu.tyut.webviewlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import edu.tyut.webviewlearn.bean.Person

@Dao
internal interface PersonDao {
    @Insert
    suspend fun insert(person: Person): Long
    @Query(value = "select * from person")
    suspend fun getPersons(): List<Person>
}