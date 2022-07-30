package com.erickferraz.appfitness.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.erickferraz.appfitness.model.App
import com.erickferraz.appfitness.R
import com.erickferraz.appfitness.model.Calc

class TmbActivity : AppCompatActivity() {

    private lateinit var selectionBox: AutoCompleteTextView
    private lateinit var editWeight: EditText
    private lateinit var editHeight: EditText
    private lateinit var editAge: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tmb)

        editWeight = findViewById(R.id.edit_tmb_weight)
        editHeight = findViewById(R.id.edit_tmb_height)
        editAge = findViewById(R.id.edit_tmb_age)
        selectionBox = findViewById(R.id.auto_complete_box_tmb)

        val items = resources.getStringArray(R.array.tmb_box_options)
        selectionBox.setText(items.first())
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        selectionBox.setAdapter(adapter)

        val btn: Button = findViewById(R.id.tmb_btn_send)
        btn.setOnClickListener {
            if(!validate()) {
                Toast.makeText(this, R.string.field_message, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weightValue = editWeight.text.toString().toInt()
            val heightValue = editHeight.text.toString().toInt()
            val ageValue = editAge.text.toString().toInt()

            val rawResult = calculateTmb(weightValue, heightValue, ageValue)
            val result = applySelectionBoxConditions(rawResult)

            AlertDialog.Builder(this)
                .setMessage(getString(R.string.tmb_response, result))
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                }
                .setNegativeButton(R.string.save) { dialog, which ->
                    Thread {
                        val app = application as App
                        val dao = app.db.calcDao()
                        val updateId = intent?.extras?.getInt("updateId")
                        when {
                            updateId != null -> dao.update(
                                Calc(id = updateId, type = "tmb", response = result))
                            else -> dao.insert(Calc(type = "tmb", response = result))
                        }
                        runOnUiThread {
                            Toast.makeText(this, R.string.saved,
                                Toast.LENGTH_SHORT).show()
                        }
                    }.start()
                }
                .create()
                .show()

            val service = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            service.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }

    private fun applySelectionBoxConditions(rawResult: Double): Double {
        val items = resources.getStringArray(R.array.tmb_box_options)
        return when {
            selectionBox.text.toString() == items[0] -> rawResult * 1.2
            selectionBox.text.toString() == items[1] -> rawResult * 1.375
            selectionBox.text.toString() == items[2] -> rawResult * 1.55
            selectionBox.text.toString() == items[3] -> rawResult * 1.725
            else -> 0.0
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
            intent.putExtra("type", "tmb")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun calculateTmb(weight: Int, height: Int, age: Int) : Double {
        return 66 + (13.8 * weight) + (5 * height) - (6.8 * age)
    }

    private fun validate() : Boolean {
        return (editWeight.text.toString().isNotEmpty() &&
                editHeight.text.toString().isNotEmpty() &&
                editAge.text.toString().isNotEmpty() &&
                !editWeight.text.toString().startsWith("0") &&
                !editHeight.text.toString().startsWith("0") &&
                !editAge.text.toString().startsWith("0"))
    }
}