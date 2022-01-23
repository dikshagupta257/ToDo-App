package com.codingblocksmodules.todoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.codingblocksmodules.todoapp.databinding.ActivityOpenedTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class OpenedTaskActivity : AppCompatActivity() {
    private lateinit var binding:ActivityOpenedTaskBinding
    private var id:Long = -1
    private lateinit var title:String
    private val db by lazy{
        AppDatabase.getDatabase(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpenedTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        //setting up the views of opened task from list
        title = intent.getStringExtra("Title").toString()
        val task = intent.getStringExtra("Task")
        val date = intent.getStringExtra("Date")
        val time = intent.getStringExtra("Time")
        id = intent.getLongExtra("Id",-1)
        binding.tvTitle.text = title
        binding.tvDate.text = date
        binding.tvTime.text = time
        binding.tvTaskDescription.text = task

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.opened_task_menu , menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.delete->{
                openDeleteDialog()
            }

            R.id.completeTask ->{
                openCompleteTaskDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //to mark the task as complete
    private fun openCompleteTaskDialog() {
        AlertDialog.Builder(this)
            .setTitle("Complete Task")
            .setMessage("Do you want to mark this task as complete?")
            .setPositiveButton("Yes"){_,_->
                GlobalScope.launch(Dispatchers.IO){
                    db.todoDao().finishTask(id)
                }
                Toast.makeText(this, "Task $title successfully completed!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel"){dialog,_->
                dialog.dismiss()
            }
            .setNeutralButton("Mark As Incomplete"){_, _ ->
                GlobalScope.launch(Dispatchers.IO){
                    db.todoDao().unFinishTask(id)
                }
                Toast.makeText(this, "Task $title marked as incomplete!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setCancelable(true)
            .create()
            .show()
    }

    //to delete the task
    private fun openDeleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Yes"){_,_->
                GlobalScope.launch(Dispatchers.IO){
                    db.todoDao().deleteTask(id)
                }
                Toast.makeText(this, "Task $title successfully deleted!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("Cancel"){dialog,_->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()
            .show()
    }
}