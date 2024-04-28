package com.example.smart_shoe

import android.content.ContentValues.TAG
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var chart: LineChart
    private lateinit var dataTextView: TextView
    private lateinit var database: DatabaseReference // firebase  distance ultrasonic
    private lateinit var database1: DatabaseReference // firebase  objects name from raspberry
    private lateinit var mediaPlayer: MediaPlayer // beebing rasp
    private lateinit var vibrator: Vibrator // vibrating data rasp
    private lateinit var vibrationSwitch: Switch
    private lateinit var soundSwitch: Switch
    private val handler = Handler()
    private var timeSeconds = 0f
    private var maxValue = 100f // Initial maximum value for y-axis
    private var isVibrationEnabled = true // Flag to track vibration state
    private var isSoundEnabled = true // Flag to track sound state
    private lateinit var hhhTextView: TextView
    private lateinit var tts: TextToSpeech
    private var currentName: String? = null // to check name

    val dialog by lazy {
        AlertDialog.Builder(this)
            .setTitle("About Project")
            .setMessage("Your project description here")
            .setPositiveButton("OK") { _, _ ->
                // Optional: Add actions if the laith clicks OK
            }
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val aboutButton: Button = findViewById(R.id.about_button)
        aboutButton.setOnClickListener {
            showAboutDialog()
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Optionally set arabic or other settings here for my project eng is enough ....
            } else {
                Log.e(TAG, "TextToSpeech initialization failed") // for handle error too
            }
        }

        // components definitions
        chart = findViewById(R.id.line_chart)
        dataTextView = findViewById(R.id.data_text_view)
        vibrationSwitch = findViewById(R.id.vibration_switch)
        soundSwitch = findViewById(R.id.sound_switch)
        hhhTextView = findViewById(R.id.hhh)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance().reference.child("distance")
        database1 = FirebaseDatabase.getInstance().reference.child("object/name")

        // Initialize chart
        setupChart()
        // Initialize MediaPlayer for custom sound
        mediaPlayer = MediaPlayer.create(this, R.raw.beeb)
        // Initialize Vibrator
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Set up listeners for switches
        setupSwitchListeners()
        // Read data from Firebase and update UI
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue(Double::class.java) ?: 0.0
                dataTextView.text = "Real-time Data: $value"
                addEntry(value.toFloat(), timeSeconds) //python send int data we must convert it ,,,
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle cancelled event
            }
        })

        database1.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val newName = dataSnapshot.getValue(String::class.java)
                if (newName != null && newName != currentName) { //remove repeated objects from rasp cam
                    currentName = newName //   check it
                    if (!shouldExcludeWord(newName)) {
                        hhhTextView.text = " $newName"
                        speakText(newName) // replacing
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here aussi
            }
        })
        // Update data every second
        handler.postDelayed(updateDataRunnable, 1000)
    }

    private fun setupChart() {
        chart.description.isEnabled = false
        chart.setTouchEnabled(true)
        chart.isDragEnabled = true
        chart.setScaleEnabled(true)
        chart.setPinchZoom(true)
        val dataSet = LineDataSet(null, "Real-time Data")
        dataSet.setDrawCircles(false)
        dataSet.setDrawValues(false)
        dataSet.color = getColor(androidx.constraintlayout.widget.R.color.accent_material_dark)
        val lineData = LineData(dataSet)
        chart.data = lineData
        // Invert the y-axis because of warning in small distance rasp
        chart.axisLeft.setInverted(true)
        // Set the initial maximum value for the y-axis
        chart.axisLeft.axisMaximum = maxValue
        chart.invalidate()
    }

    private fun addEntry(value: Float, time: Float) {
        val data = chart.data ?: return
        val set = data.getDataSetByIndex(0) as? LineDataSet ?: return
        // Add the entry to the chart
        data.addEntry(Entry(time, value), 0)
        data.notifyDataChanged()
        // Check if the value is 0 to update the maximum value for y-axis
        if (value == 0f) {
            maxValue = 100f // Set the maximum value back to 100
            chart.axisLeft.axisMaximum = maxValue
        }
        chart.notifyDataSetChanged()
        chart.moveViewToX(time)

        // Check if value is less than 90 cm for vibration and sound
        if (value < 90) {
            // Check if vibration is enabled
            if (isVibrationEnabled) {
                // Perform vibration
                performVibration()
            }
            // Check if sound is enabled
            if (isSoundEnabled) {
                // Play the "beeb" sound u can add new to ur lib
                mediaPlayer.start()
            }
        }
        timeSeconds++
    }

    private fun performVibration() {
        // Vibrate for 500 milliseconds
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            // Deprecated in API 26
            vibrator.vibrate(500)
        }
    }

    private fun setupSwitchListeners() {
        // Vibration Switch Listener
        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            isVibrationEnabled = isChecked
        }

        // Sound Switch Listener
        soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            isSoundEnabled = isChecked
        }
    }

    private val updateDataRunnable = object : Runnable {
        override fun run() {
            timeSeconds++
            handler.postDelayed(this, 1000)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateDataRunnable)
    }

    private fun speakText(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    private fun shouldExcludeWord(word: String): Boolean {
        val excludedWords = setOf("xxx", "vvv") // Add more words if needed like cane
        // val excludedLetters = "poi" // Letters to exclude rasp
        return excludedWords.contains(word.toLowerCase())
    }

    private fun showAboutDialog() {
        val message = " \n" +
                "Al-Ahliyya Amman University Amman, Jordan \n Faculty of Engineering\n" +
                "Smart Shoes for the Blind \n" +
                "“Submitted in Partial Fulfillment of the Bachelor Degree in Computer and communication Engineering”\n" +
                "Submitted by:\n" +
                "Laith otoom  \n" +
                "Under the supervision of:\n" +
                "MSc. Muneera R.M Altyeb\n" +
                "\nMy app source-code readme setup  documentation and more all on Github"
        val spannableString = SpannableString(message)
        // Define the clickable span for the Google Scholar link
        val googleScholarLink = "MSc. Muneera R.M Altyeb"
        val googleScholarUrl = "https://scholar.google.com/citations?user=2QkiqUcAAAAJ&hl=en"
        val googleScholarClickableSpan = createClickableSpan(googleScholarLink, googleScholarUrl)

        // Define the clickable span for the GitHub link
        val githubLink = "My app source-code readme setup  documentation and more all on Github"
        val githubUrl = "https://github.com/laithow1/smart-shoe"
        val githubClickableSpan = createClickableSpan(githubLink, githubUrl)

        // Apply the clickable spans to the specified ranges of text
        spannableString.setSpan(googleScholarClickableSpan, message.indexOf(googleScholarLink), message.indexOf(googleScholarLink) + googleScholarLink.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(githubClickableSpan, message.indexOf(githubLink), message.indexOf(githubLink) + githubLink.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Create the AlertDialog with the SpannableString
        val dialog = AlertDialog.Builder(this)
            .setTitle("About Project")
            .setMessage(spannableString)
            .setPositiveButton("OK") { _, _ ->
                // Optional: Add actions if the user clicks OK
            }
            .create()
        dialog.show()
        // Enable clickable links in the AlertDialog
        dialog.findViewById<TextView>(android.R.id.message)?.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun createClickableSpan(linkText: String, url: String): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Handle click on the link (open the URL)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                // Customize the appearance of the link text (optional)
                ds.isUnderlineText = true // Underline the link
                ds.color = ContextCompat.getColor(this@MainActivity, androidx.appcompat.R.color.accent_material_dark) // Set link color
            }
        }
    }



}
