package com.codealpha.fitnesstracker

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: DbHelper
    private lateinit var prefs: SharedPreferences
    private lateinit var chart: BarChartView
    private lateinit var progressBox: LinearLayout
    private lateinit var logBox: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var tvWeek: TextView
    private lateinit var rg: RadioGroup
    private val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = DbHelper(applicationContext)
        prefs = getSharedPreferences("goals", MODE_PRIVATE)
        chart = findViewById(R.id.chart)
        progressBox = findViewById(R.id.progressBox)
        logBox = findViewById(R.id.logBox)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvWeek = findViewById(R.id.tvWeek)
        rg = findViewById(R.id.rgMetric)
        findViewById<TextView>(R.id.tvDate).text =
            SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
        rg.setOnCheckedChangeListener { _, _ -> refresh() }
        findViewById<View>(R.id.fab).setOnClickListener { showAddDialog() }
        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_goals -> showGoalsDialog()
            R.id.action_clear -> AlertDialog.Builder(this)
                .setTitle(R.string.clear_all)
                .setMessage("Delete all logged activities?")
                .setPositiveButton("Delete") { _, _ -> safe { db.clearAll() }; refresh() }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun safe(block: () -> Unit) {
        try { block() } catch (e: Exception) {
            Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show()
        }
    }

    private fun num(et: EditText): Int = et.text.toString().trim().toIntOrNull() ?: 0

    private fun refresh() {
        try {
            val today = fmt.format(Date())
            val t = db.totals(today)
            progressBox.removeAllViews()
            addProgress(getString(R.string.steps), t[0], prefs.getInt("steps", 10000), "")
            addProgress(getString(R.string.calories), t[1], prefs.getInt("cal", 500), "kcal")
            addProgress(getString(R.string.minutes), t[2], prefs.getInt("min", 30), "min")

            val metric = when (rg.checkedRadioButtonId) { R.id.rbCal -> 1; R.id.rbMin -> 2; else -> 0 }
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            val dayFmt = SimpleDateFormat("EEE", Locale.getDefault())
            val vals = ArrayList<Float>()
            val labels = ArrayList<String>()
            val sum = IntArray(4)
            for (i in 0..6) {
                val d = db.totals(fmt.format(cal.time))
                for (k in 0..3) sum[k] += d[k]
                vals.add(d[metric].toFloat())
                labels.add(dayFmt.format(cal.time))
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            tvWeek.text = getString(R.string.week_summary, sum[0], sum[1], sum[2], sum[3])
            chart.setData(vals, labels)

            logBox.removeAllViews()
            val list = db.recent(20)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            for (e in list) {
                val v = layoutInflater.inflate(R.layout.item_log, logBox, false)
                v.findViewById<TextView>(R.id.tvTitle).text = "${e.type}  -  ${e.date}"
                v.findViewById<TextView>(R.id.tvSub).text =
                    "${e.minutes} min  |  ${e.calories} kcal  |  ${e.steps} steps"
                v.findViewById<View>(R.id.btnDel).setOnClickListener {
                    AlertDialog.Builder(this)
                        .setMessage("Delete this entry?")
                        .setPositiveButton("Delete") { _, _ -> safe { db.delete(e.id) }; refresh() }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
                logBox.addView(v)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Could not load data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addProgress(name: String, value: Int, goal: Int, unit: String) {
        val v = layoutInflater.inflate(R.layout.item_progress, progressBox, false)
        v.findViewById<TextView>(R.id.tvName).text = name
        v.findViewById<TextView>(R.id.tvVal).text = "$value / $goal $unit".trim()
        val pct = if (goal > 0) (value * 100L / goal).coerceAtMost(100L).toInt() else 0
        v.findViewById<ProgressBar>(R.id.bar).progress = pct
        progressBox.addView(v)
    }

    private fun showAddDialog() {
        val v = layoutInflater.inflate(R.layout.dialog_add, null)
        val sp = v.findViewById<Spinner>(R.id.spType)
        sp.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.exercise_types)
        )
        val btnDate = v.findViewById<Button>(R.id.btnDate)
        var date = fmt.format(Date())
        btnDate.text = "Date: $date"
        btnDate.setOnClickListener {
            val c = Calendar.getInstance()
            val dp = DatePickerDialog(
                this,
                { _, y, m, d ->
                    c.set(y, m, d)
                    date = fmt.format(c.time)
                    btnDate.text = "Date: $date"
                },
                c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)
            )
            dp.datePicker.maxDate = System.currentTimeMillis()
            dp.show()
        }
        val etMin = v.findViewById<EditText>(R.id.etMin)
        val etCal = v.findViewById<EditText>(R.id.etCal)
        val etSteps = v.findViewById<EditText>(R.id.etSteps)

        val dlg = AlertDialog.Builder(this)
            .setTitle(R.string.add_activity)
            .setView(v)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dlg.show()
        dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val m = num(etMin); val c = num(etCal); val s = num(etSteps)
            if (m == 0 && c == 0 && s == 0) {
                Toast.makeText(this, "Enter at least one value", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            safe { db.add(date, sp.selectedItem?.toString() ?: "Other", m, c, s) }
            dlg.dismiss()
            refresh()
        }
    }

    private fun showGoalsDialog() {
        val v = layoutInflater.inflate(R.layout.dialog_goals, null)
        val gs = v.findViewById<EditText>(R.id.gSteps)
        val gc = v.findViewById<EditText>(R.id.gCal)
        val gm = v.findViewById<EditText>(R.id.gMin)
        gs.setText(prefs.getInt("steps", 10000).toString())
        gc.setText(prefs.getInt("cal", 500).toString())
        gm.setText(prefs.getInt("min", 30).toString())
        AlertDialog.Builder(this)
            .setTitle(R.string.set_goals)
            .setView(v)
            .setPositiveButton(R.string.save) { _, _ ->
                val s = num(gs); val c = num(gc); val m = num(gm)
                prefs.edit()
                    .putInt("steps", if (s > 0) s else 10000)
                    .putInt("cal", if (c > 0) c else 500)
                    .putInt("min", if (m > 0) m else 30)
                    .apply()
                refresh()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
