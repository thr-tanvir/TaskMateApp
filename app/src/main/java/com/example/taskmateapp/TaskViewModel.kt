package com.example.taskmateapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.taskmateapp.database.TaskDatabase
import com.example.taskmateapp.model.Task
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    val allTasks: LiveData<List<Task>>

    // Set to hold the IDs of expanded tasks
    val expandedTaskIds = mutableSetOf<Int>()

    init {
        val taskDao = TaskDatabase.getDatabase(application).taskDao()
        repository = TaskRepository(taskDao)
        allTasks = repository.allTasks
    }

    fun insert(task: Task) = viewModelScope.launch { repository.insert(task) }
    fun update(task: Task) = viewModelScope.launch { repository.update(task) }
    fun delete(task: Task) = viewModelScope.launch { repository.delete(task) }
    fun search(query: String): LiveData<List<Task>> = repository.search(query)

    fun toggleExpanded(taskId: Int) {
        if (expandedTaskIds.contains(taskId)) {
            expandedTaskIds.remove(taskId)
        } else {
            expandedTaskIds.add(taskId)
        }
    }
}
