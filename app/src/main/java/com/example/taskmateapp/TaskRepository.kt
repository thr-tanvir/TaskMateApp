package com.example.taskmateapp

import androidx.lifecycle.LiveData
import com.example.taskmateapp.database.TaskDao
import com.example.taskmateapp.model.Task

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: LiveData<List<Task>> = taskDao.getAllTasks()

    suspend fun insert(task: Task) = taskDao.insertTask(task)
    suspend fun update(task: Task) = taskDao.updateTask(task)
    suspend fun delete(task: Task) = taskDao.deleteTask(task)
    fun search(query: String): LiveData<List<Task>> = taskDao.searchTasks("%$query%")
}
