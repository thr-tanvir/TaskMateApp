package com.example.taskmateapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var title: String = "",
    var description: String = "",
    var dueDate: String = "",
    var isCompleted: Boolean = false
)
