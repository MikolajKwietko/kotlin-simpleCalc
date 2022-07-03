package com.example.calc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton



class MemoryActivity : AppCompatActivity() {
    var historyString = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory)

        val memTxt1 = findViewById<TextView>(R.id.memoryTxtView1)

        for(i in 9 downTo 0){
            if(history[i].isNotEmpty()) {
                historyString = historyString + history[i] + "\n"
            }
        }
        memTxt1.text = historyString

        val fab = findViewById<FloatingActionButton>(R.id.memoryClearButton)
        fab.setOnClickListener {
            clearMemory()
        }
        val quitButton: Button = findViewById(R.id.exitButton)
        quitButton.setOnClickListener {
            finish()
        }

    }

    private fun clearMemory() {
        val memTxt1 = findViewById<TextView>(R.id.memoryTxtView1)
        memTxt1.text = " "
        historyString = ""
        history = arrayOf("","","","","","","","","","")
    }
}