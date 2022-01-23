package com.codingblocksmodules.todoapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingblocksmodules.todoapp.databinding.ActivityHistoryBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val list =  arrayListOf<TodoModel>()
    var adapter = TodoAdapter(this, list)

    private val db by lazy {
        AppDatabase.getDatabase(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        binding.completedTaskRV.apply {
            layoutManager = LinearLayoutManager(this@HistoryActivity, LinearLayoutManager.VERTICAL,true)
            adapter = this@HistoryActivity.adapter
        }

        initSwipe()
        db.todoDao().getCompletedTask().observe(this,{
            if(!it.isNullOrEmpty()){
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
            }else{
                list.clear()
                adapter.notifyDataSetChanged()
            }
        })
    }

    //util function to perform appropriate action according to the swipe made
    private fun initSwipe(){
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            //to add the functionality of swiping, according to whether the task item is left or right swiped
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if(direction == ItemTouchHelper.LEFT){
                    //delete the task if item is left swiped
                    val item = list[position]
                    GlobalScope.launch(Dispatchers.IO){
                        db.todoDao().deleteTask(adapter.getItemId(position))
                    }
                    Snackbar.make(binding.completedTaskRV, "Task ${item.title} Deleted", Snackbar.LENGTH_SHORT)
                        .setAction("Undo") {
                            GlobalScope.launch(Dispatchers.IO) {
                                db.todoDao().insertTask(item)
                            }
                            Toast.makeText(
                                this@HistoryActivity,
                                "Reinserted ${item.title} Successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.show()
                }else if(direction == ItemTouchHelper.RIGHT){
                    //un-finish the task if item is right swiped
                    val item = list[position]
                    GlobalScope.launch(Dispatchers.IO){
                        db.todoDao().unFinishTask(adapter.getItemId(position))
                    }
                    Snackbar.make(binding.completedTaskRV, "Task ${item.title} Back To Uncompleted.", Snackbar.LENGTH_SHORT)
                        .setAction("Undo") {
                            GlobalScope.launch(Dispatchers.IO) {
                                db.todoDao().finishTask(item.id)
                            }
                            Toast.makeText(
                                this@HistoryActivity,
                                "Task ${item.title} Completed.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.show()
                }
            }

            //to draw on canvas according to the swipe action
            override fun onChildDraw(
                canvas: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                //if item is swiped change the color of canvas according to the direction of swipe
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
                    val itemView = viewHolder.itemView
                    val paint = Paint()
                    val icon: Bitmap

                    if(dX>0){
                        //right swipe, make the canvas at background of yellow color
                        icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_incomplete)?.toBitmap()!!
                        paint.color = Color.parseColor("#F9D71C")
                        canvas.drawRect(itemView.left.toFloat() ,itemView.top.toFloat() ,
                            itemView.left.toFloat()+dX , itemView.bottom.toFloat() , paint)

                        canvas.drawBitmap(
                            icon,
                            itemView.left.toFloat(),
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )

                    }else{
                        //left swipe, make the canvas at background of red color
                        icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_delete)?.toBitmap()!!
                        paint.color = Color.parseColor("#D32F2F")
                        canvas.drawRect(itemView.right.toFloat()+dX , itemView.top.toFloat(),
                            itemView.right.toFloat(),itemView.bottom.toFloat(),paint)

                        canvas.drawBitmap(
                            icon,
                            itemView.right.toFloat() - icon.width,
                            itemView.top.toFloat() + (itemView.bottom.toFloat() - itemView.top.toFloat() - icon.height.toFloat()) / 2,
                            paint
                        )
                    }

                    viewHolder.itemView.translationX = dX
                }
                else{
                    //item not swiped, do nothing
                    super.onChildDraw(
                        canvas,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(binding.completedTaskRV)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.completed_task_menu , menu)
        //code for searchView
        val item = menu.findItem(R.id.search)
        val searchView = item.actionView as SearchView

        item.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                displayTodo()
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                displayTodo()
                return true
            }
        })

        //implementing search view functionality
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if(!query.isNullOrEmpty()){
                    displayTodo(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(!newText.isNullOrEmpty())
                    displayTodo(newText)
                return true
            }

        })
        return super.onCreateOptionsMenu(menu)
    }

    //util function to display list of task to do according to the name of task passed in this function
    fun displayTodo(newText:String = ""){
        db.todoDao().getCompletedTask().observe(this, {
            if(it.isNotEmpty()){
                list.clear()
                list.addAll(it.filter { todo ->
                    todo.title.contains(newText,true)
                })
                adapter.notifyDataSetChanged()
            }
        })
    }

}