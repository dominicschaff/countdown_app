package io.github.dominicschaff.countdown

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private lateinit var adapter: CoutdownAdapter
    private val coutdownTimers = ArrayList<CountdownTimer>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
        recycler_view.layoutManager = layoutManager
        adapter = CoutdownAdapter(this, coutdownTimers)
        recycler_view.adapter = adapter
        swipe_to_refresh.setOnRefreshListener { refresh() }
        fab_add.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                DatePickerDialog.OnDateSetListener { datePicker, year, month, day ->
                    TimePickerDialog(
                        this,
                        TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Set a Description")

                            val input = EditText(this)
                            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
                            builder.setView(input)

                            builder.setPositiveButton("OK") { _, _ ->
                                val c = Calendar.getInstance()
                                c.set(year, month, day, hour, minute)
                                Db(this).add(CountdownTimer(0, c.timeInMillis, input.text.toString()))
                                refresh()
                            }
                            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
                            builder.show()
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        refresh()
    }

    fun refresh() {
        swipe_to_refresh.isRefreshing = true
        Refresh(this) { result: Array<CountdownTimer> ->
            coutdownTimers.clear()
            coutdownTimers.addAll(result)
            adapter.notifyDataSetChanged()
            swipe_to_refresh.isRefreshing = false
        }.execute()
    }


    class Refresh(
        val context: Context,
        val f: (Array<CountdownTimer>) -> Unit
    ) :
        AsyncTask<Void, Void, Array<CountdownTimer>>() {
        override fun doInBackground(vararg params: Void?): Array<CountdownTimer> =
            Db(context).get()

        override fun onPostExecute(result: Array<CountdownTimer>) = f(result)
    }

    class ViewHolder(val view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.timer)
        val description: TextView = view.findViewById(R.id.description)
    }

    class CoutdownAdapter(
        private val activity: MainActivity,
        private val countdownTimers: ArrayList<CountdownTimer>
    ) :
        androidx.recyclerview.widget.RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder =
            ViewHolder(
                LayoutInflater.from(viewGroup.context).inflate(
                    R.layout.view_countdown,
                    viewGroup,
                    false
                )
            )

        override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
            val timer = countdownTimers[i]

            viewHolder.text.text = timer.farAway()
            viewHolder.description.text = timer.description
            viewHolder.view.setOnLongClickListener {
                activity.chooser("Delete?", arrayOf("No", "Yes"), callback = { action, _ ->
                    when (action) {
                        1 -> {
                            Db(activity).delete(timer)
                            activity.refresh()
                        }
                        else -> {
                        }
                    }
                })
                true
            }
        }

        override fun getItemCount(): Int = countdownTimers.size
    }
}
