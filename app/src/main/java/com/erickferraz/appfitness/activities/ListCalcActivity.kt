package com.erickferraz.appfitness.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.erickferraz.appfitness.model.App
import com.erickferraz.appfitness.R
import com.erickferraz.appfitness.model.Calc
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

class ListCalcActivity : AppCompatActivity() {

    private lateinit var rv: RecyclerView
    private lateinit var records: List<Calc>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_calc)

        records = mutableListOf()
        val adapter = CalcAdapter(records)
        rv = findViewById(R.id.rv_list_calc)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val type =
            intent?.extras?.getString("type") ?: throw IllegalStateException("type not found")

        Thread {
            val app = application as App
            val dao = app.db.calcDao()
            val response = dao.getRegisterByType(type)

            runOnUiThread {
                (records as MutableList<Calc>).addAll(response)
                adapter.notifyDataSetChanged()
            }
        }.start()
    }

    private inner class CalcAdapter(val records: List<Calc>) :
        RecyclerView.Adapter<CalcAdapter.CalcViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalcViewHolder {
            val view = layoutInflater.inflate(R.layout.result_list_item, parent, false)
            return CalcViewHolder(view)
        }

        override fun onBindViewHolder(holder: CalcViewHolder, position: Int) {
            val currentItem = records[position]

            val textView: TextView = holder.itemView.findViewById(R.id.result_txt)
            val updateBtn: ImageButton = holder.itemView.findViewById(R.id.update_btn)
            val deleteBtn: ImageButton = holder.itemView.findViewById(R.id.delete_btn)

            val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            dateFormatter.timeZone = TimeZone.getTimeZone("America/Sao_Paulo")
            val date = dateFormatter.format(currentItem.createdDate)
            val response = currentItem.response

            textView.text = getString(R.string.calc_list_response, response, date)

            deleteBtn.setOnClickListener {
                AlertDialog.Builder(this@ListCalcActivity)
                    .setMessage(R.string.delete_message)
                    .setNegativeButton("NÃO") { dialog, which ->
                        return@setNegativeButton
                    }
                    .setPositiveButton("SIM") { dialog, which ->
                        Thread {
                            val app = application as App
                            val dao = app.db.calcDao()
                            dao.delete(currentItem)

                            runOnUiThread {
                                Toast.makeText(this@ListCalcActivity, R.string.delete_success,
                                    Toast.LENGTH_SHORT).show()
                                (records as MutableList<Calc>).remove(currentItem)
                                notifyItemRemoved(position)
                            }
                        }.start()
                    }
                    .create()
                    .show()
            }

            updateBtn.setOnClickListener {
                when(intent?.extras?.getString("type")) {
                    "imc" -> {
                        val intent = Intent(this@ListCalcActivity, ImcActivity::class.java)
                        intent.putExtra("updateId", currentItem.id)
                        startActivity(intent)
                    }
                    "tmb" -> {
                        val intent = Intent(this@ListCalcActivity, TmbActivity::class.java)
                        intent.putExtra("updateId", currentItem.id)
                        startActivity(intent)
                    }
                    else -> return@setOnClickListener
                }
            }
        }

        override fun getItemCount(): Int {
            return records.size
        }

        private inner class CalcViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }
}