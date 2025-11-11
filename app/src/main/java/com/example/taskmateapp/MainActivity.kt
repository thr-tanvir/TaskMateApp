package com.example.taskmateapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.taskmateapp.model.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(), TaskAdapter.TaskItemListener {

    private lateinit var adapter: TaskAdapter
    private val taskViewModel: TaskViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutNoTasks: LinearLayout
    private lateinit var searchView: SearchView
    private lateinit var sortSpinner: Spinner

    private var currentSortOrder = SortOrder.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        recyclerView = findViewById(R.id.recyclerViewTasks)
        layoutNoTasks = findViewById(R.id.layoutNoTasks)
        searchView = findViewById(R.id.searchView)
        sortSpinner = findViewById(R.id.sortSpinner)

        setupRecyclerView()
        setupSortSpinner()
        setupSearchView()

        // Observe task list
        taskViewModel.allTasks.observe(this) { tasks ->
            updateAndSortTasks(tasks)
            toggleEmptyView(tasks.isEmpty())
        }

        val fab = findViewById<FloatingActionButton>(R.id.fabAddTask)
        fab.setOnClickListener {
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }



        setupSwipeToDelete()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(taskViewModel, this)
        recyclerView.adapter = adapter
        (recyclerView.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false
    }

    private fun setupSortSpinner() {
        val sortOptions = arrayOf("Sort by", "Title", "Due Date")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sortSpinner.adapter = sortAdapter

        sortSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                currentSortOrder = when (position) {
                    1 -> SortOrder.BY_TITLE
                    2 -> SortOrder.BY_DUE_DATE
                    else -> SortOrder.NONE
                }
                taskViewModel.allTasks.value?.let { updateAndSortTasks(it) }
            }

            override fun onNothingSelected(parent: AdapterView<*>) { /* Do nothing */ }
        }
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                taskViewModel.allTasks.value?.let { updateAndSortTasks(it) }
                return true
            }
        })
    }

    private fun updateAndSortTasks(tasks: List<Task>) {
        var filteredTasks = tasks
        val query = searchView.query.toString()
        if (query.isNotEmpty()) {
            filteredTasks = tasks.filter {
                it.title.contains(query, ignoreCase = true) || it.dueDate.contains(query, ignoreCase = true)
            }
        }

        val sortedTasks = when (currentSortOrder) {
            SortOrder.BY_TITLE -> filteredTasks.sortedBy { it.title }
            SortOrder.BY_DUE_DATE -> filteredTasks.sortedBy { it.dueDate }
            else -> filteredTasks
        }
        adapter.submitList(sortedTasks)
    }

    private fun toggleEmptyView(isEmpty: Boolean) {
        if (isEmpty) {
            layoutNoTasks.visibility = LinearLayout.VISIBLE
            recyclerView.visibility = RecyclerView.GONE
        } else {
            layoutNoTasks.visibility = LinearLayout.GONE
            recyclerView.visibility = RecyclerView.VISIBLE
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = adapter.currentList[position]
                    taskViewModel.delete(task)
                    Snackbar.make(recyclerView, "Task deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            taskViewModel.insert(task)
                        }
                        .show()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    override fun onEdit(task: Task) {
        val intent = Intent(this, AddTaskActivity::class.java)
        intent.putExtra("taskId", task.id)
        startActivity(intent)
    }

    override fun onDelete(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes") { _, _ ->
                taskViewModel.delete(task)
                Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onTaskCompleted(task: Task, isCompleted: Boolean) {
        taskViewModel.update(task.copy(isCompleted = isCompleted))
        val message = if (isCompleted) "Task marked as completed" else "Task marked as incomplete"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    enum class SortOrder {
        NONE,
        BY_TITLE,
        BY_DUE_DATE
    }
}
