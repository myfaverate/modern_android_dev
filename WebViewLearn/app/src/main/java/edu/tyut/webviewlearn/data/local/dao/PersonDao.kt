package edu.tyut.webviewlearn.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import edu.tyut.webviewlearn.bean.Person

@Dao
internal interface PersonDao {
    @Query(value = "select * from person")
    suspend fun getPersons(): List<Person>
    @Insert
    suspend fun insert(person: Person): Long
    @Delete
    suspend fun deleteById(person: Person): Int
    @Update
    suspend fun update(person: Person): Int
    @Query(value = "select * from person where id = :id")
    suspend fun getPersonById(id: Long): Person
}