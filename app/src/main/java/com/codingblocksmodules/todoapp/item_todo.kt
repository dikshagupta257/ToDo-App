package com.codingblocksmodules.todoapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingblocksmodules.todoapp.databinding.ItemTodoBinding

class item_todo : AppCompatActivity() {
    private lateinit var itemTodoBinding: ItemTodoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        itemTodoBinding = ItemTodoBinding.inflate(layoutInflater)
        setContentView(itemTodoBinding.root)
    }
}