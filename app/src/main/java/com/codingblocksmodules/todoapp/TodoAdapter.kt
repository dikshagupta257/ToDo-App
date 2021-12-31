package com.codingblocksmodules.todoapp

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codingblocksmodules.todoapp.databinding.ItemTodoBinding
import java.text.SimpleDateFormat
import java.util.*

class TodoAdapter(private val context : Context, private val list : List<TodoModel>): RecyclerView.Adapter<TodoAdapter.TodoViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            ItemTodoBinding.inflate(LayoutInflater.from(parent.context), parent , false)
        )
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        with(holder){
            with(list[position]){
                //getting list of random colors
                val colors = context.resources.getIntArray(R.array.random_color)
                val randomColor = colors[Random().nextInt(colors.size)]

                //setting up the views of item_todo layout
                itemViewBinding.viewColorTag.setBackgroundColor(randomColor)
                itemViewBinding.txtShowTitle.text = this.title
                itemViewBinding.txtShowTask.text = this.description
                itemViewBinding.txtShowCategory.text = this.category

                val myFormat1 = "EEE, d MMM yyyy"
                val sdf1 = SimpleDateFormat(myFormat1)
                itemViewBinding.txtShowDate.text = sdf1.format(Date(date))

                val myFormat2 = "h:mm a"
                val sdf2 = SimpleDateFormat(myFormat2)
                itemViewBinding.txtShowTime.text = sdf2.format(Date(time))
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long {
        return list[position].id
    }

    class TodoViewHolder(val itemViewBinding: ItemTodoBinding):RecyclerView.ViewHolder(itemViewBinding.root)

}