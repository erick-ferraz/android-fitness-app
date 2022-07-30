package com.erickferraz.appfitness.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.erickferraz.appfitness.model.App
import com.erickferraz.appfitness.R
import com.erickferraz.appfitness.model.Calc

class ImcActivity : AppCompatActivity() {

    private lateinit var editWeight: EditText
    private lateinit var editHeight: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_imc)

        editWeight = findViewById(R.id.edit_imc_weight)
        editHeight = findViewById(R.id.edit_imc_height)

        val btnSend: Button = findViewById(R.id.imc_btn_send)
        btnSend.setOnClickListener {
            if(!validate()) {
                Toast.makeText(this, R.string.field_message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weightValue = editWeight.text.toString().toInt()
            val heightValue = editHeight.text.toString().toInt()

            val imcResult = calculateImc(weightValue, heightValue)
            val imcResponseId = printImcResponse(imcResult)

            val title = getString(R.string.imc_response, imcResult)
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(imcResponseId)
                .setPositiveButton(android.R.string.ok) { dialog, which -> }
                .setNegativeButton(R.string.save) { dialog, which ->
                    Thread {
                        val app = application as App
                        val dao = app.db.calcDao()
                        val updateId = intent?.extras?.getInt("updateId")
                        when {
                            updateId != null -> dao.update(
                                Calc(id = updateId, type = "imc", response = imcResult))
                            else -> dao.insert(Calc(type = "imc", response = imcResult))
                        }
                        runOnUiThread {
                           Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT)
                        }
                    }.start()
                }
                .create()
                .show()

            val service = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            service.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.results_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.search_menu) {
            finish()
            val intent = Intent(this, ListCalcActivity::class.java)
            intent.putExtra("type", "imc")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    @StringRes
    private fun printImcResponse(imc: Double): Int {
        return when {
            imc < 15.0 -> R.string.imc_severely_low_weight
            imc < 16.0 -> R.string.imc_very_low_weight
            imc < 18.5 -> R.string.imc_low_weight
            imc < 25.0 -> R.string.normal
            imc < 30 -> R.string.imc_high_weight
            imc < 35.0 -> R.string.imc_very_high_weight
            imc < 40.0 -> R.string.imc_severely_high_weight
            else -> R.string.imc_extreme_weight
        }
    }

    private fun calculateImc(weight: Int, height: Int): Double {
        return (weight / (height/100.0 * height/100.0))
    }

    private fun validate(): Boolean {
        return (editWeight.text.toString().isNotEmpty() &&
                editHeight.text.toString().isNotEmpty() &&
                !editWeight.text.toString().startsWith("0") &&
                !editHeight.text.toString().startsWith("0"))
    }
}