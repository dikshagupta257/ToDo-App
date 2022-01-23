package com.codingblocksmodules.todoapp

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.SpinnerAdapter
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import com.codingblocksmodules.todoapp.databinding.ActivityTaskBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class TaskActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding:ActivityTaskBinding

    private lateinit var spinnerAdapter: SpinnerAdapter
    private lateinit var myCalendar:Calendar
    private lateinit var dateSetListener : DatePickerDialog.OnDateSetListener
    private lateinit var timeSetListener : TimePickerDialog.OnTimeSetListener
    private val labels = arrayListOf("Personal" , "Business" , "Insurance" ,"Shopping", "Banking")
    private var finalDate = 0L
    private var finalTime = 0L

    //initializing database to use it
    private val db by lazy{
        AppDatabase.getDatabase(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTaskBinding.inflate(layoutInflater)
        val root = binding.root
        setContentView(root)

        binding.dateEdt.setOnClickListener(this)
        binding.timeEdt.setOnClickListener(this)
        binding.saveBtn.setOnClickListener(this)
        binding.imgAddCategory.setOnClickListener {
            addCategory()
        }
        setUpSpinner()
    }

    //setting up spinner for labels
    private fun setUpSpinner() {
        spinnerAdapter = ArrayAdapter(this,
                                            android.R.layout.simple_spinner_dropdown_item,
                                            labels)

        labels.sort()
        binding.spinnerCategory.adapter = spinnerAdapter

    }

    //implementing onClick functionalities of various button in TaskActivity
    override fun onClick(view: View) {
        when(view.id){
            R.id.dateEdt -> setDateListener()
            R.id.timeEdt -> setTimeListener()
            R.id.saveBtn -> saveTodo()
        }
    }

    //to add custom category by user
    private fun addCategory() {
        binding.categoryInpLay.visibility = View.VISIBLE
    }

    //util function to save data of task entered by the user in database
    private fun saveTodo() {
        val addedCategory = binding.categoryInpLay.editText?.text.toString()
        val category: String = if(addedCategory.isNotEmpty()){
            addedCategory
        }else{
            binding.spinnerCategory.selectedItem.toString()
        }
        val title = binding.titleInpLay.editText?.text.toString()
        val description = binding.taskInpLay.editText?.text.toString()

        when {
            title.isEmpty() -> {
                binding.titleInpLay.error = "Title cannot be empty!"
            }
            description.isEmpty() -> {
                binding.taskInpLay.error = "Task cannot be empty!"
            }
            finalDate == 0L -> {
                binding.dateInpLay.error = "Please set a date."
            }
            finalTime == 0L -> {
                binding.timeInpLay.error = "Please set a time."
            }
            else -> {
                GlobalScope.launch(Dispatchers.IO) {
                    db.todoDao().insertTask(
                        TodoModel(title, description, category, finalDate, finalTime)
                    )
                }
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmShowIntent = Intent(this, MyReceiver::class.java)
                alarmShowIntent.putExtra("TodoTitle", title)
                val pendingIntent = PendingIntent.getBroadcast(this, 123, alarmShowIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                alarmManager.set(AlarmManager.RTC_WAKEUP, myCalendar.timeInMillis, pendingIntent)
                finish()
            }
        }
    }

    //to select date selected from DatePickerDialog
    private fun setDateListener() {
        myCalendar = Calendar.getInstance()
        dateSetListener = DatePickerDialog.OnDateSetListener{ _ : DatePicker, year :Int, month:Int, dayOfMonth:Int ->
            myCalendar.set(Calendar.YEAR , year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH , dayOfMonth)
            updateDate()
        }

        //date picker dialog box
        val datePickerDialog = DatePickerDialog(this , dateSetListener ,
            myCalendar.get(Calendar.YEAR) , myCalendar.get(Calendar.MONTH) , myCalendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    //set date of task according to fn setDateListener
    @SuppressLint("SimpleDateFormat")
    private fun updateDate() {
        val myFormat = "EEE, d MMM yyyy"
        val sdf = SimpleDateFormat(myFormat)  //simple date format
        binding.dateEdt.setText(sdf.format(myCalendar.time))
        finalDate = myCalendar.time.time
        binding.timeInpLay.visibility = View.VISIBLE
    }

    //to select time selected from TimePickerDialog
    private fun setTimeListener() {
        myCalendar = Calendar.getInstance()
        timeSetListener = TimePickerDialog.OnTimeSetListener{_ : TimePicker , hourOfDay : Int , min:Int ->
            myCalendar.set(Calendar.HOUR_OF_DAY , hourOfDay)
            myCalendar.set(Calendar.MINUTE , min)
            updateTime()
        }

        //time picker dialog box
        val timePickerDialog = TimePickerDialog(
            this, timeSetListener, myCalendar.get(Calendar.HOUR_OF_DAY),
            myCalendar.get(Calendar.MINUTE), false
        )
        timePickerDialog.show()
    }

    //set time of task according to fn setTimeListener
    @SuppressLint("SimpleDateFormat")
    private fun updateTime() {
        val myFormat = "h:mm a"
        val sdf = SimpleDateFormat(myFormat)
        binding.timeEdt.setText(sdf.format(myCalendar.time))
        finalTime = myCalendar.time.time
    }

}