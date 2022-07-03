package com.example.calc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlin.math.pow

var history = arrayOf("","","","","","","","","","")

class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    private var currentOperation: String = " "
    var result: Float = 0f
    private var format: String = "Float"

    private val launchSettingsActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == RESULT_OK) {
            format = result.data?.getStringExtra(getString(R.string.numberFormatKey)) ?: "Float"

            Snackbar.make(
                findViewById(R.id.mainContainer),
                format,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val spinner: Spinner = findViewById(R.id.operationSpinner)
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                val op1Button = findViewById<Button>(R.id.firstOperationButton)
                val op2Button = findViewById<Button>(R.id.secondOperationButton)

                when(position) {
                    0 -> {
                        op1Button.text = "+"
                        op2Button.text = "-"
                    }
                    1 -> {
                        op1Button.text = "*"
                        op2Button.text = "/"
                    }
                    2 -> {
                        op1Button.text = "^"
                        op2Button.text = "√"
                    }
                    else -> {
                        op1Button.text = "+"
                        op2Button.text = "-"
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        val calcButton: Button = findViewById(R.id.calcButton)
        calcButton.setOnClickListener( this )
        calcButton.setOnLongClickListener( this )

        val fab = findViewById<FloatingActionButton>(R.id.clearButton)
        fab.setOnClickListener{
            clearCalculator()
        }
    }

    private fun clearCalculator() {
        currentOperation = " "
        result = 0f
        updateOperation()
        updateResult(getString(R.string.result))
        findViewById<EditText>(R.id.firstNumber).text.clear()
        findViewById<EditText>(R.id.secondNumber).text.clear()

        Snackbar.make(
            findViewById(R.id.mainContainer),
            getString(R.string.clear_msg),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    fun SelectOperation(view: View) {
        currentOperation = (view as Button).text.toString()

        updateOperation()
    }

    private fun updateOperation() {
        val operationTxt = findViewById<TextView>(R.id.operatorSymbol)
        operationTxt.text = currentOperation
    }

    override fun onClick(p0: View?) {
        val firstNum = (findViewById<EditText>(R.id.firstNumber).text).toString().toFloatOrNull() ?: 0f
        val secondNum = (findViewById<EditText>(R.id.secondNumber).text).toString().toFloatOrNull() ?: 0f
        val resultStr: String = getResult(firstNum, secondNum)
        updateResult(resultStr)
    }

    override fun onLongClick(p0: View?): Boolean {
        val firstNum = (findViewById<EditText>(R.id.firstNumber).text).toString().toFloatOrNull() ?: 0f
        val secondNum = (findViewById<EditText>(R.id.secondNumber).text).toString().toFloatOrNull() ?: 0f
        val resultStr: String = getResult(firstNum, secondNum, updateResult = true)
        updateResult(resultStr)
        return true
    }

    private fun updateResult(resultStr: String) {
        val resultTextView = findViewById<TextView>(R.id.resultTxtView)
        resultTextView.text = resultStr
    }

    private fun getResult(
        firstNum: Float,
        secondNum: Float,
        updateResult: Boolean = false
    ): String {
        val prevResult = if (updateResult) {
            result
        } else {
            if (currentOperation.equals("/") || currentOperation.equals("*") || currentOperation.equals("√")) 1f else 0f
        }
        if(currentOperation.equals("√")){
            if(firstNum < 0f || prevResult < 0f)
                return "Error, cannot root a number less than 0."
            else if(secondNum == 0f)
                return "Error, there is no 0-th root."
        }
        if(currentOperation.equals("/")){
            if(secondNum == 0f)
                return "Error, cannot divide by 0."
        }
        if(!updateResult){

            result = when (currentOperation) {
                "+" -> firstNum + secondNum
                "-" -> firstNum - secondNum
                "*" -> firstNum * secondNum
                "/" -> firstNum / secondNum
                "^" -> firstNum.pow(secondNum)
                "√" -> firstNum.pow(1 / secondNum)
                else -> 0f
            }
        } else {
            result = when (currentOperation) {
                "+" -> prevResult + secondNum
                "-" -> prevResult - secondNum
                "*" -> prevResult * secondNum
                "/" -> prevResult / secondNum
                "^" -> prevResult.pow(secondNum)
                "√" -> prevResult.pow(1 / secondNum)

                else -> 0f
            }
        }
        updateMemory(updateResult)
        return "${getString(R.string.result)} $result"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.memoryItem -> startMemoryActivity()
            R.id.settingsItem -> startSettingsActivity()
            R.id.shareItem -> shareResult()
            else -> return super.onOptionsItemSelected(item)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareResult() {
        val shareIntent: Intent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT,getExpressionResult())
        if(shareIntent.resolveActivity(packageManager) != null){
            startActivity(shareIntent)
        }
    }

    private fun getExpressionResult(updateResult: Boolean = false): String? {
        val firstNum = (findViewById<EditText>(R.id.firstNumber).text).toString().toFloatOrNull() ?: 0f
        val secondNum = (findViewById<EditText>(R.id.secondNumber).text).toString().toFloatOrNull() ?: 0f
        if(!updateResult) {
            val equation = "${firstNum} $currentOperation ${secondNum} = "
            return when (currentOperation) {
                "+" -> "$equation ${firstNum + secondNum}"
                "-" -> "$equation ${firstNum - secondNum}"
                "*" -> "$equation ${firstNum * secondNum}"
                "/" -> "$equation ${firstNum / secondNum}"
                "^" -> "$equation ${firstNum.pow(secondNum)}"
                "√" -> "$equation ${firstNum.pow(1 / secondNum)}"
                else -> "error"
            }
        } else {
            val equation = "${result} $currentOperation ${secondNum} = "
            return when (currentOperation) {
                "+" -> "$equation ${result + secondNum}"
                "-" -> "$equation ${result - secondNum}"
                "*" -> "$equation ${result * secondNum}"
                "/" -> "$equation ${result / secondNum}"
                "^" -> "$equation ${result.pow(secondNum)}"
                "√" -> "$equation ${result.pow(1 / secondNum)}"
                else -> "error"
            }
        }
    }

    private fun updateHistory(currentOperation: String, firstNum: Float, secondNum: Float, result: Float){
        val toAdd = firstNum.toString().plus(currentOperation).plus(secondNum.toString()).plus("=").plus(result.toString())

        if(history[9].isEmpty()){
            for(i in 0..9){
                if(history[i].isEmpty()){
                    history[i] = toAdd
                    break
                }
            }
        }
        else{
            for(i in 0..9){
                if(i == 9){
                    history[9] = toAdd
                }
                else if(i != 0){
                    history[i] = history[i+1]
                }
            }
        }
    }

    private fun updateMemory(updateResult: Boolean = false){
        val resultToAdd = getExpressionResult(updateResult).toString()

        if(history[9].isEmpty()){
            for(i in 0..9){
                if(history[i].isEmpty()){
                    history[i] = resultToAdd
                    break
                }
            }
        }
        else{
            for(i in 0..8) {
                history[i] = history[i + 1]
            }
            history[9] = resultToAdd
        }
    }

    private fun startSettingsActivity() {
        val intent: Intent = Intent(this,SettingsActivity::class.java)
        intent.putExtra(getString(R.string.numberFormatKey),format)
        launchSettingsActivity.launch(intent)
    }

    private fun startMemoryActivity(){
        val intent: Intent = Intent(this, MemoryActivity::class.java)
        launchSettingsActivity.launch(intent)
    }
}