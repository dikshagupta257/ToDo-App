package com.codingblocksmodules.todoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codingblocksmodules.todoapp.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private val list = arrayListOf<TodoModel>()
    var adapter = TodoAdapter(this , list)
    private lateinit var binding:ActivityMainBinding

    val db by lazy{
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)
        setSupportActionBar(binding.toolbar)

        binding.todoRV.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL,true)
            adapter = this@MainActivity.adapter
        }

        displayTodo("")
        initSwipe()

        db.todoDao().getTask().observe(this,{
            if(!it.isNullOrEmpty()){
                list.clear()
                list.addAll(it)
                adapter.notifyDataSetChanged()
            }else{
                list.clear()
                adapter.notifyDataSetChanged()
            }
        })
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name= "todoAppChannel"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("todoAppChannel", name, importance)
            nm.createNotificationChannel(channel)
        }
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
                    Snackbar.make(binding.todoRV, "Task ${item.title} Deleted", Snackbar.LENGTH_SHORT)
                        .setAction("Undo") {
                            GlobalScope.launch(Dispatchers.IO) {
                                db.todoDao().insertTask(item)
                            }
                            Toast.makeText(
                                this@MainActivity,
                                "Reinserted ${item.title} Successfully.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.show()
                }else if(direction == ItemTouchHelper.RIGHT){
                    //finish the task if item is right swiped
                    val item = list[position]
                    GlobalScope.launch(Dispatchers.IO){
                        db.todoDao().finishTask(adapter.getItemId(position))
                    }
                    Snackbar.make(binding.todoRV, "Task ${item.title} Back To Uncompleted.", Snackbar.LENGTH_SHORT)
                        .setAction("Undo") {
                            GlobalScope.launch(Dispatchers.IO) {
                                db.todoDao().unFinishTask(item.id)
                            }
                            Toast.makeText(
                                this@MainActivity,
                                "Reinserted ${item.title} Successfully.",
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
                    val icon:Bitmap

                    if(dX>0){
                        //right swipe, make the canvas at background of green color
                        icon = ContextCompat.getDrawable(applicationContext, R.drawable.ic_check)?.toBitmap()!!
                        paint.color = Color.parseColor("#388E3C")
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
        itemTouchHelper.attachToRecyclerView(binding.todoRV)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu , menu)
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
        searchView.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
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
        db.todoDao().getTask().observe(this, {
            if(it.isNotEmpty()){
                list.clear()
                list.addAll(it.filter { todo ->
                    todo.title.contains(newText,true)
                })
                adapter.notifyDataSetChanged()
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.completedTasks ->{
                startActivity(Intent(this, HistoryActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //function to open new task activity on click of floating action button
    fun openNewTask(view: View) {
        startActivity(Intent(this, TaskActivity::class.java))
    }

}