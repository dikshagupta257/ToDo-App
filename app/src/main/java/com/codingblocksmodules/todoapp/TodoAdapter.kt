package com.codingblocksmodules.todoapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(private val context : Context, private val list : List<TodoModel>): RecyclerView.Adapter<TodoAdapter.TodoViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_todo,
                parent,
                false
            )
        , context
        )
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(list[position])

    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    class TodoViewHolder(itemView:View, val context: Context):RecyclerView.ViewHolder(itemView){
        @SuppressLint("SimpleDateFormat")
        fun bind(item:TodoModel) = with(itemView){
            val colors = context.resources.getIntArray(R.array.random_color)
            val randomColor = colors[Random().nextInt(colors.size)]

            val rvTask = findViewById<RelativeLayout>(R.id.rvTask)
            val viewColorTag = findViewById<View>(R.id.viewColorTag)
            val txtShowTitle = findViewById<TextView>(R.id.txtShowTitle)
            val txtShowTask = findViewById<TextView>(R.id.txtShowTask)
            val txtShowCategory = findViewById<TextView>(R.id.txtShowCategory)
            val txtShowDate = findViewById<TextView>(R.id.txtShowDate)
            val txtShowTime = findViewById<TextView>(R.id.txtShowTime)
            //setting up the views of item_todo layout
            viewColorTag.setBackgroundColor(randomColor)
            txtShowTitle.text = item.title
            txtShowTask.text = item.description
            txtShowCategory.text = item.category

            val myFormat1 = "EEE, d MMM yyyy"
            val sdf1 = SimpleDateFormat(myFormat1)
            txtShowDate.text = sdf1.format(Date(item.date))

            val myFormat2 = "h:mm a"
            val sdf2 = SimpleDateFormat(myFormat2)
            txtShowTime.text = sdf2.format(Date(item.time))

            val activity = itemView.context as Activity
            rvTask.setOnClickListener {
                val intent = Intent(activity, OpenedTaskActivity::class.java)
                intent.putExtra("Id", item.id)
                intent.putExtra("Title", item.title)
                intent.putExtra("Task", item.description)
                intent.putExtra("Time", sdf2.format(Date(item.time)))
                intent.putExtra("Date", sdf1.format(Date(item.date)))
                activity.startActivity(intent)
            }

        }

    }

}