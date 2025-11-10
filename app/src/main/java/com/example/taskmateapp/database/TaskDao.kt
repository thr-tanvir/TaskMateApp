package com.example.taskmateapp.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.taskmateapp.model.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): LiveData<List<Task>>

    @Insert
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE title LIKE :query OR dueDate LIKE :query ORDER BY dueDate ASC")
    fun searchTasks(query: String): LiveData<List<Task>>
}
