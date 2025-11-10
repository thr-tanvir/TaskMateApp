package com.example.taskmateapp

import android.graphics.Paint
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskmateapp.model.Task
import com.example.taskmateapp.R

class TaskAdapter(
    private val viewModel: TaskViewModel,
    private val listener: TaskItemListener
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    private val EXPAND_PAYLOAD = "EXPAND_PAYLOAD"

    interface TaskItemListener {
        fun onEdit(task: Task)
        fun onDelete(task: Task)
        fun onTaskCompleted(task: Task, isCompleted: Boolean)
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.taskTitle)
        private val description: TextView = itemView.findViewById(R.id.taskDescription)
        private val dueDate: TextView = itemView.findViewById(R.id.taskDueDate)
        private val checkboxCompleted: CheckBox = itemView.findViewById(R.id.checkboxCompleted)
        private val buttonEdit: ImageView = itemView.findViewById(R.id.buttonEdit)
        private val buttonDelete: ImageView = itemView.findViewById(R.id.buttonDelete)
        private val readMore: TextView = itemView.findViewById(R.id.readMore)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = getItem(position)
                    viewModel.toggleExpanded(task.id)
                    notifyItemChanged(position, EXPAND_PAYLOAD)
                }
            }

            buttonEdit.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEdit(getItem(position))
                }
            }

            buttonDelete.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDelete(getItem(position))
                }
            }

            checkboxCompleted.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTaskCompleted(getItem(position), checkboxCompleted.isChecked)
                }
            }
        }

        fun bind(task: Task) {
            title.text = task.title
            description.text = task.description
            dueDate.text = task.dueDate

            if (task.isCompleted) {
                title.paintFlags = title.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                title.paintFlags = title.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            checkboxCompleted.isChecked = task.isCompleted
            bindExpansion(task)
        }

        fun bindExpansion(task: Task) {
            val isExpanded = viewModel.expandedTaskIds.contains(task.id)
            if (isExpanded) {
                description.maxLines = Int.MAX_VALUE
                description.ellipsize = null
                readMore.text = "Read Less"
            } else {
                description.maxLines = 2
                description.ellipsize = TextUtils.TruncateAt.END
                readMore.text = "Read More"
            }

            description.post {
                val hasOverflow = description.lineCount > 2
                readMore.visibility = if (hasOverflow || isExpanded) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.contains(EXPAND_PAYLOAD)) {
            holder.bindExpansion(getItem(position))
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}
