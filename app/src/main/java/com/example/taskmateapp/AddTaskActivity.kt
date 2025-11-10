package com.example.taskmateapp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.example.taskmateapp.model.Task
import com.example.taskmateapp.R
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTaskActivity : AppCompatActivity() {

    private val taskViewModel: TaskViewModel by viewModels()
    private var taskId: Int = -1
    private lateinit var dueDate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val title = findViewById<TextInputEditText>(R.id.editTextTitle)
        val description = findViewById<TextInputEditText>(R.id.editTextDescription)
        dueDate = findViewById(R.id.buttonSetDueDate)
        val status = findViewById<CheckBox>(R.id.checkBoxStatus)
        val saveButton = findViewById<Button>(R.id.buttonSave)

        dueDate.setOnClickListener { showDatePickerDialog() }

        taskId = intent.getIntExtra("taskId", -1)
        if (taskId != -1) {
            supportActionBar?.title = "Edit Task"
            taskViewModel.allTasks.observe(this, object : Observer<List<Task>> {
                override fun onChanged(tasks: List<Task>) {
                    val task = tasks.find { it.id == taskId }
                    task?.let {
                        title.setText(it.title)
                        description.setText(it.description)
                        dueDate.text = it.dueDate
                        status.isChecked = it.isCompleted
                        taskViewModel.allTasks.removeObserver(this)
                    }
                }
            })
        }

        saveButton.setOnClickListener {
            val taskTitle = title.text.toString()
            val taskDescription = description.text.toString()
            val taskDueDate = dueDate.text.toString()
            val taskCompleted = status.isChecked

            if (taskTitle.isEmpty()) {
                Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (taskId == -1) {
                val newTask = Task(
                    title = taskTitle,
                    description = taskDescription,
                    dueDate = taskDueDate,
                    isCompleted = taskCompleted
                )
                taskViewModel.insert(newTask)
                Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show()
            } else {
                val updatedTask = Task(
                    id = taskId,
                    title = taskTitle,
                    description = taskDescription,
                    dueDate = taskDueDate,
                    isCompleted = taskCompleted
                )
                taskViewModel.update(updatedTask)
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dueDate.text = dateFormat.format(selectedDate.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
